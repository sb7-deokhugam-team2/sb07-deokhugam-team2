package com.deokhugam.domain.popularreview.dto.response;

import com.deokhugam.domain.base.PeriodType;

import java.time.Instant;
import java.util.UUID;

public record PopularReviewDto(
        UUID id,
        UUID reviewId,
        UUID bookId,
        String bookTitle,
        String bookThumbnailUrl,
        UUID userId,
        String userNickname,
        String reviewContent,
        Double reviewRating,
        PeriodType period,
        Instant createdAt,
        Long rank,
        Double score,
        Long likeCount,
        Long commentCount
) {
}
