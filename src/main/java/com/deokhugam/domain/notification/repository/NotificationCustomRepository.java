package com.deokhugam.domain.notification.repository;

import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.entity.Notification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationCustomRepository {

    List<Notification> searchNotifications(NotificationSearchCondition condition);

    Optional<Notification> findWithUserAndReview(UUID notificationId);

    void readAllNotifications(UUID userId);

    long deleteOldConfirmedNotifications(Instant time);

    long countByUserId(UUID userId);
}
