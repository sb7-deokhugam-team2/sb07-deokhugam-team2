package com.deokhugam.domain.review.service;

import com.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.domain.review.dto.response.ReviewDto;
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
    public ReviewDto updateReview(ReviewUpdateRequest reviewUpdateRequest, UUID requestUserId, UUID requestReviewId) {
        return null;
    }

    @Override
    public ReviewDto getReview(UUID userId, UUID reviewId) {
        return null;
    }

    @Override
    public void softDelete(UUID reviewId, UUID userId) {

    }

    @Override
    public void hardDelete(UUID reviewId) {

    }
}