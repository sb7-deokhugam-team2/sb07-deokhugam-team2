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

    @Scheduled(cron = "0 * * * * *") // 매분마다(interver)
    public void runDailyRanking() {
        popularBookService.recalculate(PeriodType.DAILY);
    }

    @Scheduled(cron = "0 * * * * *")
    public void runWeeklyRanking() {
        popularBookService.recalculate(PeriodType.WEEKLY);
    }

    @Scheduled(cron = "0 * * * * *")
    public void runMonthlyRanking() {
        popularBookService.recalculate(PeriodType.MONTHLY);
    }

    @Scheduled(cron = "0 * * * * *")
    public void runAllRanking() {
        popularBookService.recalculate(PeriodType.ALL_TIME);
    }

}
