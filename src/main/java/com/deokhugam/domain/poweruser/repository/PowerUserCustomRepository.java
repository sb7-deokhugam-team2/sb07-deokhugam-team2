package com.deokhugam.domain.poweruser.repository;

import com.deokhugam.domain.base.PeriodType;

import java.time.Instant;

public interface PowerUserCustomRepository {
    void updatePowerUserRanking(Instant time);
}
