package com.deokhugam.domain.popularbook.scheduler;

import com.deokhugam.domain.base.PeriodType;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PopularBookScheduler {

    private final JobLauncher jobLauncher;
    private final Job popularBookSnapshotJob;

    // NOTE: 주의 - popularBookQueryWindowCalculator 값과 일치하게 연산하여 스냅샷 남기도록해야 조회시 불일치 없음
    @Scheduled(cron = "0 */30 * * * *") // 30분마다(interver)
    public void runDailyRanking() throws Exception {

        runJob(PeriodType.DAILY);
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void runWeeklyRanking() throws Exception {
        runJob(PeriodType.WEEKLY);
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void runMonthlyRanking() throws Exception {
        runJob(PeriodType.MONTHLY);
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void runAllRanking() throws Exception {
        runJob(PeriodType.ALL_TIME);
    }

    private void runJob(PeriodType periodType) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("periodType", periodType.name())
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(popularBookSnapshotJob, jobParameters);

    }

}
