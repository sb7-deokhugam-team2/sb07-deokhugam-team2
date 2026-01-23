package com.deokhugam.domain.popularbook.policy;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class PopularBookQueryWindowCalculator {
    private static final Duration SNAPSHOT_INTERVAL = Duration.ofMinutes(30);

    public Instant windowStart(Instant now) {
        return now.minus(SNAPSHOT_INTERVAL);
    }
}
