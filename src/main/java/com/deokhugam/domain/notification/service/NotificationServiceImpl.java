package com.deokhugam.domain.notification.service;

import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.domain.notification.dto.response.CursorPageResponseNotificationDto;
import com.deokhugam.domain.notification.dto.response.NotificationDto;
import com.deokhugam.domain.notification.entity.Notification;
import com.deokhugam.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public NotificationDto readNotification(UUID notificationId, UUID userId, NotificationUpdateRequest notificationUpdateRequest) {
        Notification notification = notificationRepository.findWithUserAndReview(notificationId).orElseThrow(() -> new IllegalArgumentException("수정해야 함"));
        if(!notification.isOwner(userId)){
            throw new IllegalArgumentException();
        }
        if (notificationUpdateRequest.confirmed()){
            notification.confirm();
        } else {
            notification.unConfirm();
        }

        return NotificationDto.from(notification);
    }

    @Override
    public void readNotifications(UUID userId) {
        notificationRepository.readAllNotifications(userId);
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
    public void deleteNotifications(Instant time) {
        Instant weeksAgo = time.minus(7, ChronoUnit.DAYS);
        long deletedCount = notificationRepository.deleteOldConfirmedNotifications(weeksAgo);
        LocalDate date = weeksAgo.atZone(ZoneId.systemDefault()).toLocalDate();
        log.info("[NotificationService] {} 이전의 읽은 알림 정리 완료. 삭제 건수: {}", date, deletedCount);
    }
}
