package com.deokhugam.domain.comment.repository;

import com.deokhugam.domain.comment.dto.request.CommentSearchCondition;
import com.deokhugam.domain.comment.entity.Comment;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.deokhugam.domain.comment.entity.QComment.comment;
import static com.deokhugam.domain.review.entity.QReview.review;
import static com.deokhugam.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> searchComments(CommentSearchCondition condition) {

        return queryFactory.select(comment)
                .from(comment)
                .join(comment.review, review).fetchJoin()
                .join(comment.user, user).fetchJoin()
                .where(
                        comment.review.id.eq(condition.reviewId()),
                        cursorCondition(condition.cursor(), condition.direction()),
                        afterCondition(condition.after(), condition.direction())
                )
                .orderBy(direction(condition.direction()))
                .limit(condition.limit())
                .fetch();
    }

    private OrderSpecifier<?>[] direction(String direction) {
        if (direction.equals("ASC")) {
            return new OrderSpecifier[]{comment.createdAt.asc()};
        }
        return new OrderSpecifier[]{comment.createdAt.desc()};
    }

    private BooleanExpression cursorCondition(String cursor, String direction) {
        if (cursor == null) {
            return null;
        }
        Instant cursorInstant = Instant.parse(cursor);
        if (direction.equals("ASC")) return comment.createdAt.gt(cursorInstant);
        return comment.createdAt.lt(cursorInstant);
    }

    private BooleanExpression afterCondition(Instant after, String direction) {
        if (after == null) {
            return null;
        }
        if (direction.equals("ASC")) return comment.createdAt.gt(after);
        return comment.createdAt.lt(after);
    }

    @Override
    public Optional<Comment> findWithUser(UUID commentId) {
        Comment result = queryFactory
                .selectFrom(comment)
                .join(comment.user, user).fetchJoin()

                .where(comment.id.eq(commentId))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Comment> findWithUserAndReview(UUID commentId) {
        Comment result = queryFactory
                .selectFrom(comment)
                .join(comment.user, user).fetchJoin()
                .join(comment.review, review).fetchJoin()
                .where(comment.id.eq(commentId))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public long getCountByReviewId(UUID reviewId) {
        Long count = queryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.review.id.eq(reviewId))
                .fetchOne();
        return count != null ? count : 0L;
    }
}
