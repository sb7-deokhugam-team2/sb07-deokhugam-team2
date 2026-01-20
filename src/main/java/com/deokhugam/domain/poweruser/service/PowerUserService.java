package com.deokhugam.domain.poweruser.service;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.poweruser.repository.PowerUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


@Component
@RequiredArgsConstructor
public class PowerUserService {

    private final PowerUserRepository powerUserRepository;
    private final List<PeriodType> periodTypeList = List.of(PeriodType.values());

    public void calculateAndSaveRankings(ZonedDateTime time) {
        for (PeriodType periodType : periodTypeList) {
            calculateRankingByPeriod(periodType, time);
        }
    }

    private void calculateRankingByPeriod(PeriodType period, ZonedDateTime time) {
        Instant validTime = null;
        switch (period){
            case DAILY -> validTime=time.minusDays(1).toInstant();
            case WEEKLY -> validTime=time.minusWeeks(1).toInstant();
            case MONTHLY -> validTime=time.minusMonths(1).toInstant();
            default -> throw new IllegalArgumentException("지원하지 않는 기간 타입입니다.");
        }
        powerUserRepository.updatePowerUserRanking(validTime);
    }
}
