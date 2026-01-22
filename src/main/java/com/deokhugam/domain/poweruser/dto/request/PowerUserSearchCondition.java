package com.deokhugam.domain.poweruser.dto.request;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.poweruser.enums.PowerUserDirection;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record PowerUserSearchCondition(
        @NotNull
        PeriodType period,
        @NotNull
        PowerUserDirection direction,
        String cursor,
        Instant after,
        Integer limit
) {
    public PowerUserSearchCondition {
        if (limit == null) {
            limit = 50;
        }
    }
}
