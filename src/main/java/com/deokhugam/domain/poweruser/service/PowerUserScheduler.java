package com.deokhugam.domain.poweruser.service;

import com.deokhugam.domain.base.PeriodType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PowerUserScheduler {

    private final PowerUserService powerUserService;
    private final List<PeriodType> periodTypeList = List.of(PeriodType.values());

    @Scheduled(cron = "0 0 0 * * *")
    public void startRankingCalculate(){
        log.info("[PowerUserScheduler] start calculate user ranking {}", getClass());
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.DAYS);
        for (PeriodType periodType : periodTypeList) {
            powerUserService.calculateRankingByPeriod(periodType, zonedDateTime);
        }
    }
}
