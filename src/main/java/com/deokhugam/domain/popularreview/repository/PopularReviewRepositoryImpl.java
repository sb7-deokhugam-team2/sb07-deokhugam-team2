package com.deokhugam.domain.popularreview.repository;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.entity.QBook;
import com.deokhugam.domain.comment.entity.QComment;
import com.deokhugam.domain.likedreview.entity.QLikedReview;
import com.deokhugam.domain.popularreview.dto.PeriodRange;
import com.deokhugam.domain.popularreview.dto.PopularReviewListDto;
import com.deokhugam.domain.popularreview.dto.QPopularReviewListDto;
import com.deokhugam.domain.popularreview.dto.request.PopularReviewSearchCondition;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewDto;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewPageResponseDto;
import com.deokhugam.domain.popularreview.entity.PopularReview;
import com.deokhugam.domain.popularreview.entity.QPopularReview;
import com.deokhugam.domain.review.dto.request.CursorPageRequest;
import com.deokhugam.domain.review.entity.QReview;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.enums.SortDirection;
import com.deokhugam.domain.user.entity.QUser;
import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PopularReviewRepositoryImpl implements PopularReviewRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private final QLikedReview likedReview = QLikedReview.likedReview;
    private final QReview review = QReview.review;
    private final QComment comment = QComment.comment;
    private final QPopularReview popularReview = QPopularReview.popularReview;
    private final QUser user = QUser.user;
    private final QBook book = QBook.book;

    private final static int TOP = 10;
    @Override
    public List<PopularReview> calculatePopularReviews(PeriodType periodType, Instant calculatedDate) {
        PeriodRange range = PeriodRange.from(periodType, calculatedDate);
        Instant start = range.start();
        Instant end = range.end();

        Expression<Long> likedCountExpr = JPAExpressions
                .select(likedReview.id.count())
                .from(likedReview)
                .where(likedReview.review.id.eq(review.id),
                        likedReview.liked.isTrue(),
                        likedReview.updatedAt.goe(start),
                        likedReview.updatedAt.lt(end)
                );

        Expression<Long> commentCountExpr = JPAExpressions
                .select(comment.id.count())
                .from(comment)
                .where(comment.review.id.eq(review.id),
                        comment.isDeleted.isFalse(),
                        comment.createdAt.goe(start),
                        comment.createdAt.lt(end)
                );

        NumberExpression<Double> scoreExpr = Expressions.numberTemplate(
                Double.class,
                "({0} * 0.3) + ({1} * 0.7)",
                likedCountExpr,
                commentCountExpr
        );

        List<Tuple> rows = queryFactory
                .select(review, likedCountExpr, commentCountExpr, scoreExpr)
                .from(review)
                .where(
                        review.isDeleted.isFalse(),
                        scoreExpr.gt(0.0)
                )
                .orderBy(scoreExpr.desc(), review.createdAt.desc())
                .limit(TOP)
                .fetch();

        List<PopularReview> result = new ArrayList<>();

        long rank = 1;

        for (Tuple tuple : rows) {
            Review r = tuple.get(review);
            Long likedCount = tuple.get(likedCountExpr);
            Long commentCount = tuple.get(commentCountExpr);
            Double score = tuple.get(scoreExpr);

            likedCount = likedCount == null ? 0L : likedCount;
            commentCount = commentCount == null ? 0L : commentCount;
            score = score == null ? 0.0 : score;

            PopularReview popularReview = PopularReview.create(
                    periodType,
                    calculatedDate,
                    rank++,
                    score,
                    likedCount,
                    commentCount,
                    r
            );
            result.add(popularReview);
        }
            return result;
    }

    @Override
    public PopularReviewPageResponseDto searchPopularReviews(PopularReviewSearchCondition condition) {
        int limit = (condition == null || condition.limit() == null)
                ? CursorPageRequest.DEFAULT_LIMIT : condition.limit();

        int fetchSize = limit + 1;
        PeriodType period = condition == null ? null : condition.period();
        SortDirection direction = (condition == null || condition.direction() == null)
                ? SortDirection.ASC : condition.direction();
        String cursor = condition == null ? null : condition.cursor();

        if (period == null) {
            throw new DeokhugamException(ErrorCode.INVALID_REQUEST);
        }

        Instant latestCalculatedDate = queryFactory
                .select(popularReview.calculatedDate.max())
                .from(popularReview)
                .where(popularReview.periodType.eq(period))
                .fetchOne();

        if (latestCalculatedDate == null) {
            return new PopularReviewPageResponseDto(
                    List.of(),
                    null,
                    null,
                    0,
                    0L,
                    false
            );
        }

        BooleanBuilder where = new BooleanBuilder();
        where.and(popularReview.periodType.eq(period));
        where.and(popularReview.calculatedDate.eq(latestCalculatedDate));
        where.and(review.isDeleted.isFalse());

        if (cursor != null && !cursor.isBlank()) {
            long cursorRank;
            try {
                cursorRank = Long.parseLong(cursor);
            } catch (Exception e) {
                throw new DeokhugamException(ErrorCode.INVALID_REQUEST);
            }

            if (direction == SortDirection.ASC) {
                where.and(popularReview.rank.gt(cursorRank));
            } else {
                where.and(popularReview.rank.lt(cursorRank));
            }
        }

        OrderSpecifier<?>[] orderSpecifiers = direction == SortDirection.ASC
                ? new OrderSpecifier[]{popularReview.rank.asc()}
                : new OrderSpecifier[]{popularReview.rank.desc()};

        List<PopularReviewListDto> rows = queryFactory
                .select(new QPopularReviewListDto(
                        popularReview.id,
                        review.id,
                        book.id,
                        book.title,
                        book.thumbnailUrl,
                        user.id,
                        user.nickname,
                        review.content,
                        review.rating,
                        popularReview.periodType,
                        popularReview.calculatedDate,
                        popularReview.rank,
                        popularReview.score,
                        popularReview.likedCount,
                        popularReview.commentCount
                ))
                .from(popularReview)
                .join(popularReview.review, review)
                .join(review.book, book)
                .join(review.user, user)
                .where(where)
                .orderBy(orderSpecifiers)
                .limit(fetchSize)
                .fetch();

        boolean hasNext = rows.size() > limit;
        List<PopularReviewListDto> contentRows = hasNext ? rows.subList(0, limit) : rows;

        List<PopularReviewDto> content = contentRows.stream()
                .map(PopularReviewListDto::toDto)
                .toList();

        Long total = queryFactory
                .select(popularReview.count())
                .from(popularReview)
                .join(popularReview.review, review)
                .where(where)
                .fetchOne();

        long totalElements = total == null ? 0L : total;

        String nextCursor = null;
        if (hasNext && !content.isEmpty()) {
            PopularReviewDto popularReviewDto = content.get(content.size() - 1);
            nextCursor = String.valueOf(popularReviewDto.rank());
        }

        return new PopularReviewPageResponseDto(
                content,
                nextCursor,
                null,
                content.size(),
                totalElements,
                hasNext
        );
    }
}
