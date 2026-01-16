package com.deokhugam.domain.notification.repository;

import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.entity.Notification;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

import static com.deokhugam.domain.notification.entity.QNotification.notification;
import static com.deokhugam.domain.review.entity.QReview.review;
import static com.deokhugam.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class NotificationCustomRepositoryImpl implements NotificationCustomRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Notification> searchNotifications(NotificationSearchCondition condition) {
        return jpaQueryFactory.select(notification)
                .from(notification)
                .join(notification.review, review).fetchJoin()
                .join(notification.user, user).fetchJoin()
                .where(
                        notification.user.id.eq(condition.userId()),
                        notification.confirmed.isTrue(),
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
}
