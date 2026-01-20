package com.deokhugam.domain.notification.service;

import com.deokhugam.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationCreator {

    private final NotificationRepository notificationRepository;

    public void createNotification(){

    }
}
