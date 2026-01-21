package com.deokhugam.domain.review.repository;

import com.deokhugam.domain.book.entity.QBook;
import com.deokhugam.domain.comment.entity.QComment;
import com.deokhugam.domain.likedreview.entity.QLikedReview;
import com.deokhugam.domain.review.dto.QReviewListDto;
import com.deokhugam.domain.review.dto.ReviewListDto;
import com.deokhugam.domain.review.dto.request.CursorPageRequest;
import com.deokhugam.domain.review.enums.ReviewOrderBy;
import com.deokhugam.domain.review.dto.request.ReviewSearchCondition;
import com.deokhugam.domain.review.enums.SortDirection;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;
import com.deokhugam.domain.review.entity.QReview;
import com.deokhugam.domain.review.exception.ReviewInvalidException;
import com.deokhugam.domain.user.entity.QUser;
import com.deokhugam.global.exception.ErrorCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;



@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    private static final QReview review = QReview.review;
    private static final QBook book = QBook.book;
    private static final QUser user = QUser.user;
    private static final QLikedReview likedReview = QLikedReview.likedReview;
    private static final QComment comment = QComment.comment;

    @Override
    public ReviewPageResponseDto search(
            ReviewSearchCondition condition, CursorPageRequest pageRequest, UUID requestId) {
        int limit = (pageRequest == null || pageRequest.limit() == null)
                ? CursorPageRequest.DEFAULT_LIMIT
                : pageRequest.limit();

        // 다음 페이지 존재 여부(hasNext)를 판단
        int fetchSize = limit + 1;

        ReviewOrderBy orderBy = (pageRequest == null || pageRequest.orderBy() == null)
                ? ReviewOrderBy.CREATED_AT
                : pageRequest.orderBy();

        SortDirection direction = (pageRequest == null || pageRequest.direction() == null)
                ? SortDirection.DESC
                : pageRequest.direction();

        // cursor: 현재 페이지 마지막 요소 기준으로 다음 페이지 경계를 만드는 값
        String cursor = (pageRequest == null) ? null : pageRequest.cursor();

        // after: rating 정렬에서 동점 처리(tie-breaker)용 createdAt
        Instant after = (pageRequest == null) ? null : pageRequest.after();

        // 기본 필터
        BooleanBuilder where = buildWhere(condition);

        BooleanExpression cursorPredicate = buildCursorPredicate(orderBy, direction, cursor, after);

        // 커서 조건이 만들어졌다면 AND로 추가
        if (cursorPredicate != null) {
            where.and(cursorPredicate);
        }

        // orderBy가 RATING이면 tie-breaker(createdAt)가 필요함
        // rating값이 같은 row가 많으면 DB 결과 순서가 불안정해져 중복/누락 생길 수 있음
        OrderSpecifier<?>[] orderSpecifiers = buildOrderSpecifiers(orderBy, direction);

        // 좋아요 존재 여부 ( liked가 true인것만)
        BooleanExpression likedByMe = JPAExpressions
                .selectOne()
                .from(likedReview)
                .where(likedReview.review.id.eq(review.id)
                        .and(likedReview.user.id.eq(requestId))
                        .and(likedReview.liked.isTrue())
                )
                .exists();

        // 목록 조회
        // projection 안정화: @QueryProjection 기반 row 조회
        // DTO 필드 순서 바뀌면 컴파일 타임에 깨지기 때문에 사용 시 안전
        List<ReviewListDto> rows = queryFactory
                .select(toReviewListProjection(likedByMe))
                .from(review)
                .join(review.book, book) // response에 book 내용 필요
                .join(review.user, user) // response에 user 내용 필요
                .leftJoin(comment)
                .on(comment.review.id.eq(review.id)
                        .and(comment.isDeleted.isFalse())
                )
                .where(where) // 필터 + 커서 경계
                .groupBy(
                        review.id,
                        user.id,
                        book.id,
                        book.title,
                        book.thumbnailUrl,
                        review.rating,
                        user.nickname,
                        review.content,
                        review.likedCount,
                        review.createdAt,
                        review.updatedAt
                )
                .orderBy(orderSpecifiers) // 정렬 고정
                .limit(fetchSize) // limit + 1 조회
                .fetch();

        // limit보다 더 가져왔으면 다음 페이지 존재
        boolean hasNext = rows.size() > limit;

        // 실제 response content는 limit로 개수 자름
        List<ReviewListDto> contentRows = hasNext ? rows.subList(0, limit) : rows;

        // row -> API 응답 dto로 변환
        List<ReviewDto> content = contentRows.stream()
                .map(ReviewListDto::toDto)
                .toList();

        long totalElements = fetchTotalElementOptimized(condition);

        String nextCursor = null;
        Instant nextAfter = null;

        // 다음 페이지가 있을 때만 nextCursor/after를 내려준다.
        if (hasNext && !content.isEmpty()) {
            ReviewDto reviewDto = content.get(content.size() - 1);

            if (orderBy == ReviewOrderBy.RATING) {
                // rating 정렬: rating 커서 + createdAt(after) 필요
                nextCursor = reviewDto.rating() == null ? null : String.valueOf(reviewDto.rating());
                nextAfter = reviewDto.createdAt();
            } else {
                // createdAt 정렬: createdAt 커서만 있으면 createdAt이 곧 경계
                nextCursor = reviewDto.createdAt() == null ? null : reviewDto.createdAt().toString();
            }
        }
        return new ReviewPageResponseDto(
                content,
                nextCursor,
                nextAfter,
                content.size(),
                totalElements,
                hasNext
        );
    }


    @Override
    public Optional<ReviewDto> findDetail(UUID reviewId, UUID requestUserId) {
        BooleanExpression likedByMe = JPAExpressions
                .selectOne()
                .from(likedReview)
                .where(likedReview.review.id.eq(reviewId)
                        .and(likedReview.user.id.eq(requestUserId))
                        .and(likedReview.liked.isTrue())
                )
                .exists();

        Expression<Long> commentCount = JPAExpressions
                .select(comment.id.count())
                .from(comment)
                .where(comment.review.id.eq(reviewId)
                        .and(comment.isDeleted.isFalse())
                );

        ReviewDto reviewDto = queryFactory
                .select(Projections.constructor(
                        ReviewDto.class,
                        review.id,
                        review.user.id,
                        review.book.id,
                        review.book.title,
                        review.book.thumbnailUrl,
                        review.rating,
                        review.user.nickname,
                        review.content,
                        review.likedCount,
                        commentCount,
                        likedByMe,
                        review.createdAt,
                        review.updatedAt
                ))
                .from(review)
                .join(review.user, user)
                .join(review.book, book)
                .where(review.id.eq(reviewId)
                        .and(review.isDeleted.isFalse()))
                .fetchOne();
        return Optional.ofNullable(reviewDto);
    }

    @Override
    public void addLikedCount(UUID reviewId, long delta) {
        queryFactory.update(review)
                .set(review.likedCount, review.likedCount.add(delta))
                .where(review.id.eq(reviewId)
                        .and(review.isDeleted.isFalse()))
                .execute();
    }

    /**
     * 기본 WHERE 조건(필터)
     * - soft delete 리뷰는 기본 제외(isDeleted=false)
     * - userId/bookId는 완전일치 필터
     * - keyword는 부분일치(닉네임/내용/도서제목)
     */
    private BooleanBuilder buildWhere(ReviewSearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder();

        // soft delete 제외 적용
        // 포함해야한다면 분기 해야함
        where.and(review.isDeleted.isFalse());

        if (condition == null) {
            return where;
        }

        // userId 필터(완전 일치)
        if (condition.userId() != null) {
            where.and(review.user.id.eq(condition.userId()));
        }

        // bookId 필터(완전일치)
        if (condition.bookId() != null) {
            where.and(review.book.id.eq(condition.bookId()));
        }

        // keyword 필터(부분일치)
        if (StringUtils.hasText(condition.keyword())) {
            String keyword = condition.keyword().trim();
            where.and(user.nickname.containsIgnoreCase(keyword)
                    .or(review.content.containsIgnoreCase(keyword))
                    .or(book.title.containsIgnoreCase(keyword))
            );
        }
        return where;
    }

    /**
     * 커서 조건
     * createdAt 정렬:
     * - cursor(Instant)만 있으면 충분
     * rating 정렬:
     * - cursor(Double) + after(Instant)가 있어야 동점 처리가 가능
     */
    private BooleanExpression buildCursorPredicate(
            ReviewOrderBy orderBy, SortDirection direction, String cursor, Instant after) {

        // cursor가 없으면 첫 페이지 -> 커서 조건 없음
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        // rating 정렬 커서 : cursor가 rating이고 after는 createdAt
        if (orderBy == ReviewOrderBy.RATING) {
            // cursor 문자열을 Double로 파싱
            Double cursorRating = null;
            try {
                cursorRating = Double.valueOf(cursor);
            } catch (NumberFormatException e) {
                throw new ReviewInvalidException(ErrorCode.REVIEW_INVALID);
            }

            // rating 정렬에서는 after가 반드시 필요
            if (after == null) {
                throw new ReviewInvalidException(ErrorCode.REVIEW_INVALID);
            }

            // DESC: rating이 더 낮거나, rating이 같은데 createdAt이 더 과거인 데이터
            if (direction == SortDirection.DESC) {
                return review.rating.lt(cursorRating)
                        .or(review.rating.eq(cursorRating)
                                .and(review.createdAt.lt(after)));
            }
            // ASC: rating이 더 높거나, rating이 같은데 createdAt이 더 미래인 데이터
            else {
                return review.rating.gt(cursorRating)
                        .or(review.rating.eq(cursorRating)
                                .and(review.createdAt.gt(after)));
            }
        }

        // createdAt 정렬 커서: cursor가 createdAt(Instant 문자열)
        Instant cursorCreatedAt = null;
        try {
            cursorCreatedAt = Instant.parse(cursor);
        } catch (Exception e) {
            throw new ReviewInvalidException(ErrorCode.REVIEW_INVALID);
        }

        // DESC: createdAt이 cursor보다 이전인 데이터만 다음 페이지로
        if (direction == SortDirection.DESC) {
            return review.createdAt.lt(cursorCreatedAt);
        }
        // ASC: createdAt이 cursor보다 미래인 데이터만 다음 페이지로
        else {
            return review.createdAt.gt(cursorCreatedAt);
        }
    }

    /**
     * ORDER BY 구성
     * - createdAt 정렬
     * - rating 정렬 (+ tie-breaker: createdAt)
     */
    private OrderSpecifier<?>[] buildOrderSpecifiers(ReviewOrderBy orderBy, SortDirection direction) {

        boolean desc = direction == SortDirection.DESC;

        // rating 정렬은 동점이 많아서 createdAt 2차 정렬(tie-breaker)이 필수
        if (orderBy == ReviewOrderBy.RATING) {
            return desc ? new OrderSpecifier[]{
                    review.rating.desc(), review.createdAt.desc()
            }
                    : new OrderSpecifier[]{
                    review.rating.asc(), review.createdAt.asc()
            };
        }

        // createdAt 정렬은 createdAt 단독으로 충분
        return desc ? new OrderSpecifier[]{
                review.createdAt.desc()
        }
                : new OrderSpecifier[]{
                review.createdAt.asc()
        };
    }

    /**
     * @QueryProjection 기반 Projection
     * 장점으로 constructor는 컴파일 오류를 잡지 못하고 런타임 오류를 잡으나 어노테이션을 활용하면 컴파일 오류로 잡음
     * 단점으로 어노테이션을 쓰기 때문에 QueryDSL에 종속, DTO까지 Q객체로 생성됨
     */
    private QReviewListDto toReviewListProjection(BooleanExpression likedByMe) {
        return new QReviewListDto(
                review.id,
                user.id,
                book.id,
                book.title,
                book.thumbnailUrl,
                review.rating,
                user.nickname,
                review.content,
                review.likedCount,
                comment.id.count(),
                likedByMe,
                review.createdAt,
                review.updatedAt
        );
    }

    /**
     * totalElements 최적화
     * - keyword가 없을 때는 join 없이 count
     * - keyword가 있을 때만 join count 수행
     */
    private long fetchTotalElementOptimized(ReviewSearchCondition condition) {
        // soft delete 제외
        BooleanBuilder where = new BooleanBuilder();
        where.and(review.isDeleted.isFalse());

        // condition이 없으면 전체 count
        if (condition == null) {
            Long total = queryFactory
                    .select(review.count())
                    .from(review)
                    .where(where)
                    .fetchOne();
            return total == null ? 0L : total;
        }

        // userId/bookId 필터는 join 없이 가능
        if (condition.userId() != null) {
            where.and(review.user.id.eq(condition.userId()));
        }
        if (condition.bookId() != null) {
            where.and(review.book.id.eq(condition.bookId()));
        }

        // keyword가 없으면 join 없이 count
        if (!StringUtils.hasText(condition.keyword())) {
            Long total = queryFactory
                    .select(review.count())
                    .from(review)
                    .where(where)
                    .fetchOne();
            return total == null ? 0L : total;
        }

        // keyword가 있으면 nickname/title 필터 때문에 join count 필요
        String keyword = condition.keyword().trim();

        BooleanBuilder keywordWhere = new BooleanBuilder(where);
        keywordWhere.and(user.nickname.containsIgnoreCase(keyword)
                .or(review.content.containsIgnoreCase(keyword))
                .or(book.title.containsIgnoreCase(keyword))
        );

        Long total = queryFactory
                .select(review.count())
                .from(review)
                .join(review.user, user)
                .join(review.book, book)
                .where(keywordWhere)
                .fetchOne();
        return total == null ? 0L : total;
    }
}

