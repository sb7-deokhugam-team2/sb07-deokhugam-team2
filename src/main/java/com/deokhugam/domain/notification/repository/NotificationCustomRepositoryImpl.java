package com.deokhugam.domain.notification.repository;

import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.entity.Notification;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.deokhugam.domain.notification.entity.QNotification.notification;
import static com.deokhugam.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class NotificationCustomRepositoryImpl implements NotificationCustomRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notification> searchNotifications(NotificationSearchCondition condition) {
        return List.of();
    }

    @Override
    public Optional<Notification> findWithUserAndReview(UUID notificationId) {
        Notification result = queryFactory
                .select(notification)
                .from(notification)
                .join(notification.user, user).fetchJoin()
                .join(notification.review, notification.review).fetchJoin()
                .where(notification.id.eq(notificationId))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public void readAllNotifications(UUID userId) {
        queryFactory
                .update(notification)
                .set(notification.confirmed, true)
                .set(notification.updatedAt, Instant.now()) //영속성 컨텍스트를 거치지 않기 때문에 직접 값을 넣어줘야 한다
                .where(notification.user.id.eq(userId))
                .execute();
        em.clear();
    }

    @Override
    public long deleteOldConfirmedNotifications(Instant time) {
        long count = queryFactory
                .delete(notification)
                .where(
                        notification.confirmed.isTrue(),
                        notification.updatedAt.before(time)
                )
                .execute();
        em.clear();
        return count;
    }
}
