package com.deokhugam.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 0 * * *", zone = "#{T(java.util.TimeZone).getDefault().getID()}")
    public void deleteNotifications(){
        log.info("[NotificationScheduler] start deleteNotifications {}", getClass());
        Instant midnight = ZonedDateTime.now(ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.DAYS)
                .toInstant();
        notificationService.deleteNotifications(midnight);
    }
}
