package com.deokhugam.domain.notification.batch;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class NotificationJobMetricsListener implements JobExecutionListener {

    private final MeterRegistry meterRegistry;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        meterRegistry.counter(
                "batch_notification_job_started_total",
                "job", jobExecution.getJobInstance().getJobName()
        ).increment();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        BatchStatus status = jobExecution.getStatus();

        meterRegistry.counter(
                "batch_notification_job_completed_total",
                "job", jobName,
                "status", status.name()
        ).increment();

        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();

        if (startTime != null && endTime != null) {
            long durationMs = Duration.between(startTime, endTime).toMillis();

            Timer.builder("batch_notification_job_duration_ms")
                    .tag("job", jobName)
                    .tag("status", status.name())
                    .register(meterRegistry)
                    .record(durationMs, TimeUnit.MILLISECONDS);
        }
    }
}
