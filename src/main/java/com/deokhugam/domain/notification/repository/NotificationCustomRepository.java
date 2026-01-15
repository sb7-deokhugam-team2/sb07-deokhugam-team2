package com.deokhugam.domain.notification.repository;

import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.entity.Notification;

import java.util.List;

public interface NotificationCustomRepository {

    List<Notification> searchNotifications(NotificationSearchCondition condition);
}
