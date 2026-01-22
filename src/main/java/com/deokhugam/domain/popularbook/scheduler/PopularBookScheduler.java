package com.deokhugam.domain.popularbook.scheduler;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.popularbook.service.PopularBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PopularBookScheduler {

    private final PopularBookService popularBookService;

    // NOTE: 주의 - popularBookQueryWindowCalculator 값과 일치하게 연산하여 스냅샷 남기도록해야 조회시 불일치 없음
    @Scheduled(cron = "0 */30 * * * *") // 30분마다(interver)
    public void runDailyRanking() {
        popularBookService.snapshotPopularBooks(PeriodType.DAILY);
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void runWeeklyRanking() {
        popularBookService.snapshotPopularBooks(PeriodType.WEEKLY);
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void runMonthlyRanking() {
        popularBookService.snapshotPopularBooks(PeriodType.MONTHLY);
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void runAllRanking() {
        popularBookService.snapshotPopularBooks(PeriodType.ALL_TIME);
    }

}
