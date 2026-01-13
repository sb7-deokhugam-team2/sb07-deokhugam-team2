package com.deokhugam.domain.review.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ReviewSearchCondition(
        @NotNull
        UUID userId,

        @NotNull
        UUID bookId,

        @Size(max = 100)
        String keyword
) {
}
