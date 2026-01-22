package com.deokhugam.domain.poweruser.service;

import com.deokhugam.domain.base.PeriodType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PowerUserScheduler {

    private final PowerUserService powerUserService;
    private final Duration buffer = Duration.ofSeconds(5);
    private final List<PeriodType> periodTypeList = List.of(PeriodType.values());

    @Scheduled(cron = "0 5 0 * * *",  zone = "#{T(java.util.TimeZone).getDefault().getID()}")
    public void startRankingCalculate(){

        log.info("[PowerUserScheduler] start calculate user ranking {}", getClass());

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())
                .plus(buffer)
                .truncatedTo(ChronoUnit.DAYS);

        for (PeriodType periodType : periodTypeList) {
            try {
                log.info("[PowerUserScheduler] {} 집계 시작 (기준 시점: {})", periodType, zonedDateTime);
                powerUserService.calculateRankingByPeriod(periodType, zonedDateTime);
                log.info("[Scheduler] {} 집계 완료.", periodType);
            } catch (Exception e) {
                log.warn("[PowerUserScheduler] {} 집계 실패! 다음 작업을 진행합니다.", periodType, e);
            }
        }
    }
}
