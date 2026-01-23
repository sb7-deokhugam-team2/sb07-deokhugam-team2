package com.deokhugam.domain.popularreview.scheduler;

import com.deokhugam.domain.popularreview.service.PopularReviewBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PopularReviewScheduler {
    private final PopularReviewBatchService popularReviewBatchService;
    @Scheduled(cron = "0 */30 * * * *", zone = "Asia/Seoul")
    public void runDaily() {
        log.info("[PopularReviewScheduler] Start");
        popularReviewBatchService.calculateAndSaveAllPeriods();
        log.info("[PopularReviewScheduler] End");
    }
}
