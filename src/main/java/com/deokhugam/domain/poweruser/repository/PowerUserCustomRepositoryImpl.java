package com.deokhugam.domain.poweruser.repository;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.poweruser.dto.request.PowerUserSearchCondition;
import com.deokhugam.domain.poweruser.entity.PowerUser;
import com.deokhugam.domain.poweruser.entity.QPowerUser;
import com.deokhugam.domain.poweruser.enums.PowerUserDirection;
import com.deokhugam.domain.poweruser.repository.dto.UserCommentCountDto;
import com.deokhugam.domain.poweruser.repository.dto.UserLikeCountDto;
import com.deokhugam.domain.poweruser.repository.dto.UserReviewScoreDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.deokhugam.domain.comment.entity.QComment.comment;
import static com.deokhugam.domain.likedreview.entity.QLikedReview.likedReview;
import static com.deokhugam.domain.poweruser.entity.QPowerUser.powerUser;
import static com.deokhugam.domain.review.entity.QReview.review;
import static com.deokhugam.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class PowerUserCustomRepositoryImpl implements PowerUserCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PowerUser> searchPowerUsers(PowerUserSearchCondition condition) {

        Instant startOfDay = getLatestInstant(condition.period());

        return queryFactory
                .selectFrom(powerUser)
                .join(powerUser.user, user).fetchJoin()
                .where(
                        powerUser.calculatedDate.goe(startOfDay),
                        powerUser.periodType.eq(condition.period()),
                        cursorCondition(condition.cursor(), condition.direction()),
                        afterCondition(condition.after(), condition.direction())
                )
                .orderBy(direction(condition.direction()))
                .limit(condition.limit()+1)
                .fetch();
    }

    private OrderSpecifier<?>[] direction(PowerUserDirection direction) {
        if (direction==PowerUserDirection.ASC){
            return new OrderSpecifier[]{powerUser.rank.asc()};
        }
        return new OrderSpecifier[]{powerUser.rank.desc()};
    }

    private BooleanExpression cursorCondition(String cursor, PowerUserDirection direction) {
        if (cursor == null) {
            return null;
        }
        Instant cursorInstant = Instant.parse(cursor);
        if (direction==PowerUserDirection.ASC) return powerUser.createdAt.gt(cursorInstant);
        return powerUser.createdAt.lt(cursorInstant);
    }

    private BooleanExpression afterCondition(Instant after, PowerUserDirection direction) {
        if (after == null) {
            return null;
        }
        if (direction==PowerUserDirection.ASC) return powerUser.createdAt.gt(after);
        return powerUser.createdAt.lt(after);
    }

    @Override
    public Map<UUID, Long> getUserLikedCount(Instant time) {
        List<UserLikeCountDto> result = queryFactory
                .select(Projections.constructor(
                                UserLikeCountDto.class,
                                likedReview.user.id,
                                likedReview.count()
                        )
                )
                .from(likedReview)
                .where(likedReviewCreatedAtGoe(time))
                .groupBy(likedReview.user.id)
                .fetch();

        return result.stream().collect(Collectors.toMap(
                UserLikeCountDto::getUserId,
                UserLikeCountDto::getLikedCount,
                (existing, replacement) -> existing));
    }

    @Override
    public Map<UUID, Long> getUserCommentCount(Instant time) {
        List<UserCommentCountDto> result = queryFactory
                .select(Projections.constructor(
                                UserCommentCountDto.class,
                                comment.user.id,
                                comment.count()
                        )
                )
                .from(comment)
                .where(commentCreatedAtGoe(time))
                .groupBy(comment.user.id)
                .fetch();

        return result.stream().collect(Collectors.toMap(
                UserCommentCountDto::getUserId,
                UserCommentCountDto::getCommentCount,
                (existing, replacement) -> existing));
    }

    @Override
    public Map<UUID, Double> getUserReviewScore(Instant time) {
        List<UserReviewScoreDto> result = queryFactory
                .select(Projections.constructor(
                                UserReviewScoreDto.class,
                                review.user.id,
                                comment.id.countDistinct().castToNum(Double.class).multiply(0.7)
                                        .add(likedReview.id.countDistinct().castToNum(Double.class).multiply(0.3))
                        )
                )
                .from(review)
                .leftJoin(comment).on(
                        comment.review.id.eq(review.id).and(
                        commentCreatedAtGoe(time))
                )
                .leftJoin(likedReview).on(
                        likedReview.review.id.eq(review.id).and(
                        likedReviewCreatedAtGoe(time))
                )
                .where(
                        comment.id.isNotNull().or(likedReview.id.isNotNull())
                )
                .groupBy(review.user.id)
                .fetch();
        return result.stream().collect(Collectors.toMap(
                UserReviewScoreDto::getUserId,
                UserReviewScoreDto::getScore,
                (existing, replacement) -> existing));
    }

    private BooleanExpression commentCreatedAtGoe(Instant time) {
        return time == null ? null : comment.createdAt.goe(time);
    }

    private BooleanExpression likedReviewCreatedAtGoe(Instant time) {
        return time == null ? null : likedReview.createdAt.goe(time);
    }

    @Override
    public Long countByPeriodTypeAndCalculatedDate(PeriodType periodType) {
        Instant latestInstant = getLatestInstant(periodType);
        if (latestInstant == null) {
            return 0L;
        }

        Long result = queryFactory
                .select(powerUser.count())
                .from(powerUser)
                .where(
                        powerUser.calculatedDate.goe(latestInstant),
                        powerUser.periodType.eq(periodType)
                )
                .fetchOne();
        return result != null ? result : 0L;
    }

    private Instant getLatestInstant(PeriodType periodType) {
        Instant latestInstant = queryFactory
                .select(powerUser.calculatedDate.max())
                .from(powerUser)
                .where(powerUser.periodType.eq(periodType))
                .fetchOne();
        if (latestInstant == null) return null;
        ZonedDateTime startOfDay = latestInstant.atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS);
        return startOfDay.toInstant();
    }
}
