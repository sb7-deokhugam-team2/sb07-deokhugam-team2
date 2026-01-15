package com.deokhugam.domain.notification.repository;

import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.entity.Notification;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class NotificationCustomRepositoryImpl implements NotificationCustomRepository{
    private JPAQueryFactory jpaQueryFactory;
    @Override
    public List<Notification> searchNotifications(NotificationSearchCondition condition) {
        return List.of();
    }
}
