package com.deokhugam.domain.notification.scheduler;

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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final JobLauncher jobLauncher;
    private final Job notificationJob;

    @Scheduled(cron = "0 */30 * * * *")
    public void deleteNotifications() {
        log.info("[NotificationScheduler] start deleteNotifications {}", getClass());

        Instant time = ZonedDateTime.now(ZoneId.systemDefault())
                .toInstant();

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetTime", time.toString())
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(notificationJob, jobParameters);
        } catch (Exception e) {
            log.error("[NotificationScheduler] 삭제 배치 실행 중 에러 발생", e);
        }
    }
}
