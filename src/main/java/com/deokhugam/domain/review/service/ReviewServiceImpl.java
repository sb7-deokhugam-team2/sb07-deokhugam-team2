package com.deokhugam.domain.review.service;

import com.deokhugam.domain.review.dto.request.CursorPageRequest;
import com.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.domain.review.dto.request.ReviewSearchCondition;
import com.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    @Override
    public ReviewDto createReview(ReviewCreateRequest reviewCreateRequest, UUID requestUserId) {
        return null;
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