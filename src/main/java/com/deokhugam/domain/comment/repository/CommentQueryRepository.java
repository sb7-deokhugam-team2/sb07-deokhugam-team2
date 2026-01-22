package com.deokhugam.domain.comment.repository;

import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static com.deokhugam.domain.comment.entity.QComment.comment;
import static com.deokhugam.domain.review.entity.QReview.review;
import static com.deokhugam.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<CommentDto> findCommentDto(UUID commentId) {
        CommentDto commentDto = queryFactory
                .select(Projections.constructor(
                        CommentDto.class,
                        comment.id,
                        comment.review.id,
                        comment.user.id,
                        comment.user.nickname,
                        comment.content,
                        comment.createdAt,
                        comment.updatedAt
                ))
                .from(comment)
                .where(
                        comment.isDeleted.isFalse(),
                        comment.id.eq(commentId)
                )
                .join(comment.review, review)
                .join(comment.user, user)
                .fetchOne();
        return Optional.ofNullable(commentDto);
    }
}
