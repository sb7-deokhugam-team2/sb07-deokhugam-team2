package com.deokhugam.domain.poweruser.batch;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.poweruser.service.PowerUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PowerUserBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PowerUserService powerUserService;

    @Bean
    public Job PowerUserRankingJob(Step calculateRankingByPeriod, PowerUserJobMetricsListener metricsListener) {
        return new JobBuilder("powerUserRankingJob", jobRepository)
                .start(calculateRankingByPeriod)
                .listener(metricsListener)
                .build();
    }

    @Bean
    public Step calculateRankingByPeriod() {
        return new StepBuilder("calculatePowerUserRanking", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    Map<String, Object> params = chunkContext.getStepContext().getJobParameters();

                    String period = (String) params.get("periodType");
                    PeriodType periodType = PeriodType.valueOf(period);

                    String targetDateStr = (String) params.get("targetZoneDateTime");
                    ZonedDateTime targetZonedDateTime = (targetDateStr != null)
                            ? ZonedDateTime.parse(targetDateStr)
                            : ZonedDateTime.now(ZoneId.systemDefault());

                    powerUserService.calculateRankingByPeriod(periodType, targetZonedDateTime);
                    return RepeatStatus.FINISHED;

                }, transactionManager)
                .build();
    }
}