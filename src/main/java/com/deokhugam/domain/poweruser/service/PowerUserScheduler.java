package com.deokhugam.domain.poweruser.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PowerUserScheduler {

    private final PowerUserService powerUserService;

    @Scheduled(cron = "0 0 0 * * *")
    public void startRankingCalculate(){
        log.info("[PowerUserScheduler] start calculate user ranking {}", getClass());
        powerUserService.calculateAndSaveRankings();
    }
}
