package com.deokhugam.domain.poweruser.repository;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.poweruser.repository.dto.ReviewScoreDto;
import com.deokhugam.domain.poweruser.repository.dto.UserCommentCountDto;
import com.deokhugam.domain.poweruser.repository.dto.UserLikeCountDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.deokhugam.domain.comment.entity.QComment.comment;
import static com.deokhugam.domain.likedreview.entity.QLikedReview.likedReview;
import static com.deokhugam.domain.review.entity.QReview.review;
import static com.deokhugam.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class PowerUserCustomRepositoryImpl implements  PowerUserCustomRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public void updatePowerUserRanking(PeriodType periodType) {

        switch (periodType){
            case PeriodType.DAILY ->
        }
    }

    private void getUserLikedCount() {
        List<UserLikeCountDto> result = queryFactory
                .select(Projections.constructor(
                                UserLikeCountDto.class,
                                user.id,
                                likedReview.count()
                        )
                )
                .from(user)
                .join(likedReview).on(
                        user.id.eq(likedReview.user.id),
                        likedReview.createdAt.goe()
                .groupBy(user.id)
                .fetch();
    }

    public void getUserCommentCount() {
        List<UserCommentCountDto> result = queryFactory
                .select(Projections.constructor(
                                UserCommentCountDto.class,
                                user.id,
                                likedReview.count()
                        )
                )
                .from(user)
                .join(comment).on(
                        user.id.eq(comment.user.id),
                        comment.createdAt.goe(Instant.now().minus(7, ChronoUnit.DAYS)))
                .groupBy(user.id)
                .fetch();
    }

    public void test3() {
        List<ReviewScoreDto> fetch = queryFactory
                .select(Projections.constructor(
                                ReviewScoreDto.class,
                                review.id,
                                comment.count()
                        )
                )
                .from(review)
                .leftJoin(comment).on(
                        comment.review.id.eq(review.id)
                )
                .groupBy(review.id)
                .fetch();
    }
}
