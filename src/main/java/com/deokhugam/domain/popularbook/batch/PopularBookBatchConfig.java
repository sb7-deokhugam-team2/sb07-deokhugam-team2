package com.deokhugam.domain.popularbook.batch;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.popularbook.service.PopularBookService;
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

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PopularBookBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PopularBookService popularBookService;

    @Bean
    public Job popularBookSnapshotJob(Step popularBookSnapshotStep, PopularBookJobMetricsListener metricsListener) {
        return new JobBuilder("popularBookSnapshotJob", jobRepository)
                .start(popularBookSnapshotStep)
                .listener(metricsListener)
                .build();
    }

    @Bean
    public Step popularBookSnapshotStep() {
        return new StepBuilder("popularBookSnapshotStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
                    String period = (String) jobParameters.get("periodType");
                    PeriodType periodType = PeriodType.valueOf(period);
                    popularBookService.snapshotPopularBooks(periodType);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }


}
