package com.deokhugam.domain.popularreview.dto;

import com.deokhugam.domain.base.PeriodType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record PeriodRange(
        Instant start,
        Instant end
) {
    public static PeriodRange from(PeriodType periodType, Instant calculatedDate, Instant now) {
        return switch(periodType) {
            case DAILY ->  new PeriodRange(calculatedDate, now);
            case WEEKLY -> new PeriodRange(now.minus(7, ChronoUnit.DAYS),now);
            case MONTHLY -> new PeriodRange(now.minus(30, ChronoUnit.DAYS),now);
            case ALL_TIME ->  new PeriodRange(Instant.EPOCH, now);
        };
    }
}
