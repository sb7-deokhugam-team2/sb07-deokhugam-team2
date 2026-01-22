package com.deokhugam.domain.likedreview.dto.response;

import java.util.UUID;

public record LikedReviewDto(
        UUID reviewId,
        UUID userId,
        boolean liked
) {
}
