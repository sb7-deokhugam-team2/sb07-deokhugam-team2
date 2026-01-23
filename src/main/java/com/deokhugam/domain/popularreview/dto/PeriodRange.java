package com.deokhugam.domain.popularreview.dto;

import com.deokhugam.domain.base.PeriodType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record PeriodRange(
        Instant start,
        Instant end
) {
    public static PeriodRange from(PeriodType periodType, Instant calculatedDate) {
        return switch(periodType) {
            case DAILY ->  new PeriodRange(calculatedDate, calculatedDate.plus(1, ChronoUnit.DAYS));
            case WEEKLY -> new PeriodRange(calculatedDate.minus(7, ChronoUnit.DAYS),calculatedDate);
            case MONTHLY -> new PeriodRange(calculatedDate.minus(30, ChronoUnit.DAYS),calculatedDate);
            case ALL_TIME ->  new PeriodRange(Instant.EPOCH, calculatedDate);
        };
    }
}
