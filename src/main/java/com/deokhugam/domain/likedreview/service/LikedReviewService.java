package com.deokhugam.domain.likedreview.service;

import com.deokhugam.domain.likedreview.dto.response.LikedReviewDto;

import java.util.UUID;

public interface LikedReviewService {
    LikedReviewDto toggleLike(UUID reviewId, UUID userId);
}
