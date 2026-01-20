package com.deokhugam.domain.poweruser.repository;

import com.deokhugam.domain.poweruser.repository.dto.ReviewScoreDto;
import com.deokhugam.domain.poweruser.repository.dto.UserCommentCountDto;
import com.deokhugam.domain.poweruser.repository.dto.UserLikeCountDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.deokhugam.domain.comment.entity.QComment.comment;
import static com.deokhugam.domain.likedreview.entity.QLikedReview.likedReview;
import static com.deokhugam.domain.review.entity.QReview.review;
import static com.deokhugam.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class PowerUserCustomRepositoryImpl implements PowerUserCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public void updatePowerUserRanking(Instant time) {
        Map<UUID, Long> userLikedCount = getUserLikedCount(time);
        Map<UUID, Long> userCommentCount = getUserCommentCount(time);
    }

    private Map<UUID, Long> getUserLikedCount(Instant time) {

        List<UserLikeCountDto> result = queryFactory
                .select(Projections.constructor(
                                UserLikeCountDto.class,
                                likedReview.user.id,
                                likedReview.count()
                        )
                )
                .from(likedReview)
                .where(likedReview.createdAt.goe(time))
                .groupBy(likedReview.user.id)
                .fetch();

        return result.stream().collect(Collectors.toMap(
                UserLikeCountDto::getUserId,
                UserLikeCountDto::getLikedCount,
                (existing, replacement) -> existing));
    }

    public Map<UUID, Long> getUserCommentCount(Instant time) {
        List<UserCommentCountDto> result = queryFactory
                .select(Projections.constructor(
                                UserCommentCountDto.class,
                                comment.user.id,
                                comment.count()
                        )
                )
                .from(comment)
                .where(comment.createdAt.goe(time))
                .groupBy(comment.user.id)
                .fetch();

        return result.stream().collect(Collectors.toMap(
                UserCommentCountDto::getUserId,
                UserCommentCountDto::getCommentCount,
                (existing, replacement) -> existing));
    }

    public void test3(Instant time) {
        List<ReviewScoreDto> fetch = queryFactory
                .select(Projections.constructor(
                                ReviewScoreDto.class,
                                review.id,
                                review.user.id,
                                comment.count().multiply(0.3)
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
