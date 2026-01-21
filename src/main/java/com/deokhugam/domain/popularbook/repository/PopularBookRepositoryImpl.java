package com.deokhugam.domain.popularbook.repository;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.enums.SortDirection;
import com.deokhugam.domain.popularbook.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.popularbook.dto.response.PopularBookAggregationDto;
import com.deokhugam.domain.popularbook.dto.response.PopularBookDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static com.deokhugam.domain.book.entity.QBook.book;
import static com.deokhugam.domain.popularbook.entity.QPopularBook.popularBook;
import static com.deokhugam.domain.review.entity.QReview.review;

@RequiredArgsConstructor
public class PopularBookRepositoryImpl implements PopularBookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PopularBookAggregationDto> findTopPopularBookAggregates(PeriodType periodType, int limit) {
        Instant now = Instant.now();
        Instant from = calculateFrom(now, periodType);

        NumberExpression<Long> reviewCount = review.count();
        NumberExpression<Double> reviewRatingAvg = review.rating.avg();
        NumberExpression<Double> score = reviewCount.doubleValue().multiply(0.4)
                .add(
                        reviewRatingAvg.coalesce(0.0).multiply(0.6)
                );

        return queryFactory
                .select(
                        Projections.constructor(
                                PopularBookAggregationDto.class,
                                review.book.id,
                                reviewCount,
                                reviewRatingAvg.coalesce(0.0),
                                score
                        )
                )
                .from(review)
                .join(review.book, book) // NOTE: select 필드컬럼으로는 쓰고있지않아 필요없지만, 논리삭제 필터에 사용한 book.isDeleted.isFalse() 체크가히기위해 isDeleted 컬럼 값 사용
                .where(
                        review.isDeleted.isFalse(),
                        review.createdAt.lt(now),
                        review.createdAt.goe(from),
                        book.isDeleted.isFalse()
                )
                .groupBy(review.book.id)
                .orderBy(
                        score.desc(),
                        book.createdAt.desc(),
                        book.id.asc()
                )
                .limit(limit)
                .fetch();
    }

    @Override
    public Page<PopularBookDto> findTopPopularBooks(PopularBookSearchCondition condition, Instant windowStart, Pageable pageable) {
        int size = pageable.getPageSize();
        PeriodType periodType = condition.period();
        JPQLQuery<Instant> latestCalculatedDateSubquery =
                JPAExpressions
                        .select(popularBook.calculatedDate.max())
                        .from(popularBook)
                        .where(
                                popularBook.periodType.eq(periodType), // 기간타입이 같고
                                popularBook.calculatedDate.goe(windowStart) // 윈도우시작보다 큰(원하는 범위의 최신 행들만)값인 경우
                        );
        List<PopularBookDto> popularBookDtoList = queryFactory
                .select(Projections.constructor(
                                PopularBookDto.class,
                                popularBook.id,
                                book.id,
                                book.title,
                                book.author,
                                book.thumbnailUrl,
                                popularBook.periodType,
                                popularBook.rank,
                                popularBook.score,
                                popularBook.reviewCount,
                                popularBook.rating,
                                popularBook.createdAt
                        )
                )
                .from(popularBook)
                .join(popularBook.book, book)
                .where(
                        popularBook.periodType.eq(periodType),
                        popularBook.calculatedDate.eq(latestCalculatedDateSubquery),
                        cursorWherePredicate(condition)
                )
                .orderBy(
                        condition.direction() == SortDirection.ASC ? popularBook.rank.asc() : popularBook.rank.desc(),
                        condition.direction() == SortDirection.ASC ? popularBook.createdAt.asc() : popularBook.createdAt.desc()
                )
                .limit(size + 1)
                .fetch();

        boolean hasNext = popularBookDtoList.size() > size;
        if (hasNext) {
            popularBookDtoList.remove(size); // hasNext를 위해 하나더 가져온 부분 제거
        }

        Long total = Optional.ofNullable(
                queryFactory
                        .select(popularBook.count().coalesce(0L)) // 없을때 fetchOne으로 null이 오는걸 방지하기위한 0
                        .from(popularBook)
                        .where(
                                popularBook.periodType.eq(periodType),
                                popularBook.calculatedDate.eq(latestCalculatedDateSubquery)
                        )
                        .fetchOne()
        ).orElse(0L); // 반환타입과 PageImpl 타입을 맞추기위해 Optional 사용

        return new PageImpl<>(popularBookDtoList, pageable, total);
    }

    private BooleanExpression cursorWherePredicate(PopularBookSearchCondition condition) {
        String cursor = condition.cursor();
        Instant after = condition.after();
        if (condition.cursor() == null || condition.after() == null) return null;

        boolean isDirectionAsc = condition.direction() == SortDirection.ASC;

        return isDirectionAsc ?
                popularBook.rank.gt(Long.valueOf(cursor))
                        .or(
                                popularBook.rank.eq(Long.valueOf(cursor)).and(popularBook.createdAt.gt(after))
                        )
                :
                popularBook.rank.lt(Long.valueOf(cursor))
                        .or(
                                popularBook.rank.eq(Long.valueOf(cursor)).and(popularBook.createdAt.lt(after))
                        );
    }

    private Instant calculateFrom(Instant now, PeriodType periodType) {
        return switch (periodType) {
            case DAILY -> now.minus(1, ChronoUnit.DAYS);
            case WEEKLY -> now.minus(7, ChronoUnit.DAYS);
            case MONTHLY -> now.minus(30, ChronoUnit.DAYS);
            case ALL_TIME -> Instant.EPOCH; // 전체 기간을 EPOCH 시작점으로하여 모든 기간
        };

    }
}
