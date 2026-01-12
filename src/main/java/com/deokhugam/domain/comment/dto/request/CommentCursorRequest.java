package com.deokhugam.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CommentCursorRequest(
        @NotNull
        UUID reviewId,

        @NotBlank
        String direction,

        String cursor,

        Instant after,

        Integer limit
) {
    public CommentCursorRequest {
        if (limit == null) {
            limit = 50;
        }
    }
}
