package com.deokhugam.domain.review.dto.request;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ReviewSearchCondition(
        UUID userId,
        UUID bookId,

        @Size(max = 100)
        String keyword
) {
}
