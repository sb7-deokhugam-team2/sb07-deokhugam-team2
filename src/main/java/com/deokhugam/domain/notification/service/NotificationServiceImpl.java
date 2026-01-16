package com.deokhugam.domain.notification.service;

import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.domain.notification.dto.response.CursorPageResponseNotificationDto;
import com.deokhugam.domain.notification.dto.response.NotificationDto;
import com.deokhugam.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public NotificationDto readNotification(UUID notificationId, UUID userId, NotificationUpdateRequest notificationUpdateRequest) {
        return null;
    }

    @Override
    public void readNotifications(UUID userId) {

    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseNotificationDto getNotifications(NotificationSearchCondition condition) {
        List<NotificationDto> notifications = notificationRepository.searchNotifications(condition)
                .stream()
                .map(NotificationDto::from)
                .collect(Collectors.toList());

        boolean hasNext = notifications.size() > condition.limit();
        if(notifications.size() > condition.limit()){
            notifications.remove(notifications.size() - 1);
        }

        String nextCursor = null;
        Instant nextAfter = null;

        if(!notifications.isEmpty()){
            NotificationDto lastItem = notifications.get(notifications.size() - 1);
            nextCursor = lastItem.getCreatedAt().toString();
            nextAfter = lastItem.getCreatedAt();
        }

        long totalElements = notificationRepository.count();

        return CursorPageResponseNotificationDto.from(
                notifications,
                nextCursor,
                nextAfter,
                notifications.size(),
                totalElements,
                hasNext
        );
    }

    @Override
    public void deleteNotifications() {

    }
}
