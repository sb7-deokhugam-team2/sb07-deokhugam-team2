package com.deokhugam.domain.review.service;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.comment.repository.CommentRepository;
import com.deokhugam.domain.review.dto.request.CursorPageRequest;
import com.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.domain.review.dto.request.ReviewSearchCondition;
import com.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.enums.ReviewOrderBy;
import com.deokhugam.domain.review.exception.ReviewAccessDeniedException;
import com.deokhugam.domain.review.exception.ReviewAlreadyExistsException;
import com.deokhugam.domain.review.exception.ReviewInvalidException;
import com.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.deokhugam.domain.review.mapper.ReviewMapper;
import com.deokhugam.domain.review.mapper.ReviewUrlMapper;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final CommentRepository commentRepository;
    private final ReviewUrlMapper reviewUrlMapper;

    @Override
    public ReviewDto createReview(ReviewCreateRequest request) {

        // Book 엔티티 조회
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        // User 엔티티 조회
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 도서별 하나의 활성 리뷰만 등록 가능
        if(reviewRepository.existsReviewByUserIdAndBookId(request.userId(), request.bookId())) {
            throw new ReviewAlreadyExistsException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // Review 객체 생성 후 저장
        Review review = Review.create(request.rating(), request.content(), book, user);
        review = reviewRepository.save(review);

        return reviewMapper.toReviewDto(review, 0L, false);

    }

    @Override
    public ReviewDto updateReview(ReviewUpdateRequest request, UUID requestUserId, UUID reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        // 논리 삭제된 리뷰는 수정 불가
        if (review.isDeleted()) {
            throw new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND);
        }

        // 본인이 작성한 리뷰만 수정 가능
        if(!review.getUser().getId().equals(requestUserId)) {
            throw new ReviewAccessDeniedException(ErrorCode.REVIEW_ACCESS_DENIED);
        }

        review.update(request.rating(), request.content());

        ReviewDto reviewDetail = reviewRepository.findDetail(reviewId, requestUserId)
                .orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        return reviewUrlMapper.withFullThumbnailUrl(reviewDetail);

    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDto getReview(UUID requestUserId, UUID reviewId) {
        ReviewDto reviewDto = reviewRepository.findDetail(reviewId, requestUserId)
                .orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        return reviewUrlMapper.withFullThumbnailUrl(reviewDto);
    }

    @Override
    public void softDeleteReview(UUID reviewId, UUID requestUserId) {

        Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(requestUserId)) {
            throw new ReviewAccessDeniedException(ErrorCode.REVIEW_ACCESS_DENIED);
        }

        review.delete();
        reviewRepository.save(review);

    }

    @Override
    public void hardDeleteReview(UUID reviewId, UUID requestUserId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        reviewRepository.delete(review);

    }

    @Override
    @Transactional(readOnly = true)
    public ReviewPageResponseDto searchReviews(ReviewSearchCondition condition, CursorPageRequest pageRequest, UUID requestId) {
            validateCursor(pageRequest);
        ReviewPageResponseDto search = reviewRepository.search(condition, pageRequest, requestId);

        List<ReviewDto> reviewDtoList = reviewUrlMapper.withFullThumbnailUrl(search.content());

        return new ReviewPageResponseDto(
                reviewDtoList,
                search.nextCursor(),
                search.nextAfter(),
                search.size(),
                search.totalElements(),
                search.hasNext()
        );
    }

    private void validateCursor(CursorPageRequest pageRequest) {
        if (pageRequest == null) {
            return;
        }

        String cursor = pageRequest.cursor();
        if (cursor == null || cursor.isBlank()) {
            return;
        }

        ReviewOrderBy orderBy = pageRequest.orderBy() == null
                ? ReviewOrderBy.CREATED_AT : pageRequest.orderBy();

        if (orderBy == ReviewOrderBy.RATING && pageRequest.after() == null) {
            throw new ReviewInvalidException(ErrorCode.REVIEW_AFTER_REQUIRED);
        }

        try {
            if (pageRequest.orderBy() == ReviewOrderBy.RATING) {
                Double.valueOf(cursor);
            } else {
                Instant.parse(cursor);
            }
        } catch (Exception e) {
            throw new ReviewInvalidException(ErrorCode.REVIEW_INVALID);
        }
    }
}