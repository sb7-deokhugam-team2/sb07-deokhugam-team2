package com.deokhugam.domain.notification.service;

import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.domain.notification.dto.response.CursorPageResponseNotificationDto;
import com.deokhugam.domain.notification.dto.response.NotificationDto;

import java.time.Instant;
import java.util.UUID;

public interface NotificationService {

    NotificationDto readNotification(UUID notificationId, UUID userId, NotificationUpdateRequest notificationUpdateRequest);

    void readNotifications(UUID userId);

    CursorPageResponseNotificationDto getNotifications(NotificationSearchCondition condition);

    void deleteNotifications(Instant time);
}
