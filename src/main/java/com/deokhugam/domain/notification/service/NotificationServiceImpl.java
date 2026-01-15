package com.deokhugam.domain.notification.service;

import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.domain.notification.dto.response.CursorPageResponseNotificationDto;
import com.deokhugam.domain.notification.dto.response.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    @Override
    public NotificationDto readNotification(UUID notificationId, UUID userId, NotificationUpdateRequest notificationUpdateRequest) {
        return null;
    }

    @Override
    public void readNotifications(UUID userId) {

    }

    @Override
    public CursorPageResponseNotificationDto getNotifications(NotificationSearchCondition condition) {
        return null;
    }

    @Override
    public void deleteNotifications() {

    }
}
