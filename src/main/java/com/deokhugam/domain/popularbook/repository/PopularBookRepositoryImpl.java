package com.deokhugam.domain.popularbook.repository;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.popularbook.dto.response.PopularBookAggregationDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.deokhugam.domain.book.entity.QBook.book;
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

    private static Instant calculateFrom(Instant now, PeriodType periodType) {
        return switch (periodType) {
            case DAILY -> now.minus(1, ChronoUnit.DAYS);
            case WEEKLY -> now.minus(7, ChronoUnit.DAYS);
            case MONTHLY -> now.minus(30, ChronoUnit.DAYS);
            case ALL_TIME -> Instant.EPOCH; // 전체 기간을 EPOCH 시작점으로하여 모든 기간
        };

    }
}
