package com.deokhugam.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record NotificationSearchCondition(
        @NotNull
        UUID userId,

        @NotBlank
        String direction,

        String cursor,

        Instant after,

        Integer limit
) {
    public NotificationSearchCondition {
        if (limit == null) {
            limit = 20;
        }
    }
}
