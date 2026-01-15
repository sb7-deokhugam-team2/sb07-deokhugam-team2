package com.deokhugam.domain.review.service;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.review.dto.request.CursorPageRequest;
import com.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.domain.review.dto.request.ReviewSearchCondition;
import com.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.exception.ReviewAlreadyExistsException;
import com.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.deokhugam.domain.review.mapper.ReviewMapper;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    // todo: LikedReviewRepository
    @Override
    @Transactional
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
    public ReviewDto updateReview(ReviewUpdateRequest reviewUpdateRequest, UUID requestUserId, UUID reviewId) {
        return null;
    }

    @Override
    public ReviewDto getReview(UUID requestUserId, UUID reviewId) {
        return null;
    }

    @Override
    public void softDelete(UUID reviewId, UUID requestUserId) {

    }

    @Override
    public void hardDelete(UUID reviewId, UUID requestUserId) {

    }

    @Override
    public ReviewPageResponseDto searchReviews(ReviewSearchCondition condition, CursorPageRequest pageRequest, UUID requestId) {
        return null;
    }
}