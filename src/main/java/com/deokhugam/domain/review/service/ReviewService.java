package com.deokhugam.domain.review.service;

import com.deokhugam.domain.review.dto.request.CursorPageRequest;
import com.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.domain.review.dto.request.ReviewSearchCondition;
import com.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;

import java.util.UUID;

public interface ReviewService {

    ReviewDto createReview(ReviewCreateRequest reviewCreateRequest, UUID requestUserId);

    ReviewDto updateReview(ReviewUpdateRequest reviewUpdateRequest, UUID requestUserId, UUID reviewId);

    ReviewDto getReview(UUID requestUserId, UUID reviewId);

    void softDelete(UUID reviewId, UUID requestUserId);

    void hardDelete(UUID reviewId, UUID requestUserId);

    ReviewPageResponseDto searchReviews(ReviewSearchCondition condition, CursorPageRequest pageRequest, UUID requestId);

}
