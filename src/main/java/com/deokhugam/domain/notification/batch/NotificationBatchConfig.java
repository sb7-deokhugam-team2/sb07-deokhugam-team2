package com.deokhugam.domain.notification.batch;

import com.deokhugam.domain.notification.service.NotificationService;
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

import java.time.Instant;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class NotificationBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NotificationService notificationService;

    @Bean
    public Job notificationJob(Step notificationDelete, NotificationJobMetricsListener metricsListener) {
        return new JobBuilder("notificationJob", jobRepository)
                .start(notificationDelete)
                .listener(metricsListener)
                .build();
    }

    @Bean
    public Step notificationDelete() {
        return new StepBuilder("notificationDelete", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Map<String, Object> params = chunkContext.getStepContext().getJobParameters();

                    String instantStr = (String) params.get("targetTime");
                    Instant time = (instantStr != null) ? Instant.parse(instantStr) : Instant.now();
                    notificationService.deleteNotifications(time);

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
