package com.deokhugam.domain.notification.repository;

import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.entity.Notification;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.deokhugam.domain.notification.entity.QNotification.notification;
import static com.deokhugam.domain.user.entity.QUser.user;
import static com.deokhugam.domain.review.entity.QReview.review;

@RequiredArgsConstructor
public class NotificationCustomRepositoryImpl implements NotificationCustomRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notification> searchNotifications(NotificationSearchCondition condition) {
        return queryFactory.select(notification)
                .from(notification)
                .join(notification.review, review).fetchJoin()
                .join(notification.user, user).fetchJoin()
                .where(
                        notification.user.id.eq(condition.userId()),
                        cursorCondition(condition.cursor(), condition.direction()),
                        afterCondition(condition.after(), condition.direction())
                )
                .orderBy(direction(condition.direction()))
                .limit(condition.limit()+1)
                .fetch();
    }

    private OrderSpecifier<?>[] direction(String direction) {
        if(direction.equals("ASC")){
            return new OrderSpecifier[]{notification.createdAt.asc()};
        }
        return new OrderSpecifier[]{notification.createdAt.desc()};
    }

    private BooleanExpression cursorCondition(String cursor, String direction) {
        if(cursor == null){
            return null;
        }
        Instant cursorInstant = Instant.parse(cursor);
        if(direction.equals("ASC")) return notification.createdAt.gt(cursorInstant);
        return notification.createdAt.lt(cursorInstant);
    }

    private BooleanExpression afterCondition(Instant after, String direction) {
        if(after == null){
            return null;
        }
        if(direction.equals("ASC")) return notification.createdAt.gt(after);
        return notification.createdAt.lt(after);
    }

    @Override
    public Optional<Notification> findWithUserAndReview(UUID notificationId) {
        Notification result = queryFactory
                .select(notification)
                .from(notification)
                .join(notification.user, user).fetchJoin()
                .join(notification.review, review).fetchJoin()
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

    @Override
    public long countByUserId(UUID userId) {
        Long elements = queryFactory
                .select(notification.count())
                .from(notification)
                .where(notification.user.id.eq(userId))
                .fetchOne();
        return elements != null ? elements : 0L;
    }
}
