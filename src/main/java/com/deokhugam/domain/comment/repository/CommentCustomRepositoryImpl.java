package com.deokhugam.domain.comment.repository;

import com.deokhugam.domain.comment.entity.Comment;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.deokhugam.domain.comment.entity.QComment.*;
import static com.deokhugam.domain.review.entity.QReview.*;
import static com.deokhugam.domain.user.entity.QUser.*;

@Repository
@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> findCommentByCursor(UUID reviewId, String direction, String cursor, Instant after, Integer limit) {

        return queryFactory.select(comment)
                .from(comment)
                .join(comment.review, review)
                .join(comment.user, user)
                .where(afterGt(after))
                .orderBy(direction(direction))
                .fetch();
    }

    private OrderSpecifier<?>[] direction(String direction) {
        if(direction.equals("ASC")){
            return new OrderSpecifier[]{ comment.createdAt.asc() };
        }
        return new OrderSpecifier[]{ comment.createdAt.desc() };
    }

    private BooleanExpression afterGt(Instant after) {
        if(after == null){
            return null;
        }

        return comment.createdAt.gt(after);
    }
}
