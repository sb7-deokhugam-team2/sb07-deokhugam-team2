package com.deokhugam.domain.review.dto.request;

import java.util.UUID;

public record ReviewCreateRequest(
        UUID bookId,
        UUID userId,
        Double rating,
        String content
) {
}
