package com.deokhugam.domain.poweruser.scheduler;

import com.deokhugam.domain.base.PeriodType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PowerUserScheduler {

    private final JobLauncher jobLauncher;
    private final Job PowerUserRankingJob;

    @Scheduled(cron = "0 */30 * * * *")
    public void startRankingCalculate() {

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        for (PeriodType type : PeriodType.values()) {
            try {
                runPowerUserRankingJob(type, now);
            } catch (Exception e) {
                log.error("[PowerUserScheduler] {} 랭킹 배치 실행 중 에러 발생", type, e);
            }
        }
    }

    private void runPowerUserRankingJob(PeriodType periodType, ZonedDateTime now)
            throws JobExecutionAlreadyRunningException,
            JobRestartException,
            JobInstanceAlreadyCompleteException,
            JobParametersInvalidException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("periodType", periodType.toString())
                .addString("targetZoneDateTime", now.toString())
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(PowerUserRankingJob, jobParameters);
    }
}
