package com.deokhugam.domain.likedreview.service;

import com.deokhugam.domain.likedreview.dto.response.LikedReviewDto;
import com.deokhugam.domain.likedreview.entity.LikedReview;
import com.deokhugam.domain.likedreview.repository.LikedReviewRepository;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.exception.UserNotFoundException;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LikedReviewServiceImpl implements LikedReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final LikedReviewRepository likedReviewRepository;

    @Override
    public LikedReviewDto toggleLike(UUID reviewId, UUID userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        if (review.isDeleted()) {
            throw new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));

        Optional<LikedReview> opt = likedReviewRepository.findByReviewIdAndUserId(reviewId, userId);

        if (opt.isPresent()) {
            return toggleExisting(opt.get(), reviewId, userId);
        }

        try {
            LikedReview likedReview  = LikedReview.create(review, user);
            likedReviewRepository.saveAndFlush(likedReview);

            reviewRepository.addLikedCount(reviewId, +1);
            return new LikedReviewDto(reviewId, userId, true);
        } catch (DataIntegrityViolationException e) { // 유니크 제약 위반 시 예외
            LikedReview likedReview = likedReviewRepository.findByReviewIdAndUserId(reviewId, userId)
                    .orElseThrow(() -> e);
            return toggleExisting(likedReview, reviewId, userId);
        }
    }

    private LikedReviewDto toggleExisting(LikedReview likedReview, UUID reviewId, UUID userId) {
        boolean before = likedReview.isLiked();
        boolean after = likedReview.toggle();

        long delta = 0;
        if (!before && after) {
            delta = delta + 1;
        }
        else if (before && !after) {
            delta = delta - 1;
        }

        if (delta != 0) {
            reviewRepository.addLikedCount(reviewId,delta);
        }

        return new LikedReviewDto(reviewId,userId,after);
    }
}
