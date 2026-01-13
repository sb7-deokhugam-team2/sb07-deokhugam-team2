package com.deokhugam.domain.review.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record ReviewCreateRequest(
        @NotNull
        UUID bookId,

        @NotNull
        UUID userId,

        @NotNull
        @DecimalMax("5.0")
        @DecimalMin(value = "0.0", inclusive = false)
        Double rating,

        @NotBlank
        @Size(max = 500)
        String content
) {
}
