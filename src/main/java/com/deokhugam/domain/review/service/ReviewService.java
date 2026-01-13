package com.deokhugam.domain.review.service;

import com.deokhugam.domain.review.dto.request.CursorPageRequest;
import com.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.domain.review.dto.request.ReviewSearchCondition;
import com.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface ReviewService {

    ReviewDto createReview(ReviewCreateRequest reviewCreateRequest, UUID requestUserId);

    ReviewDto updateReview(ReviewUpdateRequest reviewUpdateRequest, UUID requestUserId, UUID requestReviewId);

    ReviewDto getReview(UUID userId, UUID reviewId);

    void softDelete(UUID reviewId,UUID userId);

    void hardDelete(UUID reviewId);

    ReviewPageResponseDto searchReviews(ReviewSearchCondition condition, CursorPageRequest pageRequest, UUID requestId);

}
