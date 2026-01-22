package com.deokhugam.domain.review.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReviewDto(
        UUID id,
        UUID userId,
        UUID bookId,
        String bookTitle,
        String bookThumbnailUrl,
        Double rating,
        String userNickname,
        String content,
        Long likeCount,
        Long commentCount,
        Boolean likedByMe,
        Instant createdAt,
        Instant updatedAt
) {
}