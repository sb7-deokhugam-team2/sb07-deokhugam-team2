package com.deokhugam.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteNotifications(){
        log.info("[NotificationScheduler] start deleteNotifications {}", getClass());
        notificationService.deleteNotifications();
    }
}
