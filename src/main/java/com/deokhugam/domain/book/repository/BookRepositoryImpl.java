package com.deokhugam.domain.book.repository;

import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.enums.SortDirection;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.deokhugam.domain.book.entity.QBook.book;
import static com.deokhugam.domain.review.entity.QReview.review;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<BookDto> findBookDetailById(UUID bookId) {
        BookDto dto = queryFactory
                .select(Projections.constructor(
                        BookDto.class,
                        book.id,
                        book.title,
                        book.author,
                        book.description,
                        book.publisher,
                        book.publishedDate,
                        book.isbn,
                        book.thumbnailUrl,
                        review.id.countDistinct().coalesce(0L),
                        review.rating.avg().coalesce(0.0),
                        book.createdAt,
                        book.updatedAt
                ))
                .from(book)
                .leftJoin(review).on(review.book.id.eq(book.id))
                .where(book.id.eq(bookId))
                .groupBy(
                        book.id,
                        book.title,
                        book.author,
                        book.description,
                        book.publisher,
                        book.publishedDate,
                        book.isbn,
                        book.thumbnailUrl,
                        book.createdAt,
                        book.updatedAt
                )
                .fetchOne();

        return Optional.ofNullable(dto);
    }

    @Override
    public Slice<BookDto> findBooks(BookSearchCondition condition, Pageable pageable) {
        int size = pageable.getPageSize();
        Instant after = parseAfter(condition.after());

        NumberExpression<Long> reviewCount = review.id.countDistinct().coalesce(0L);
        NumberExpression<Double> avgRating = review.rating.avg().coalesce(0.0);

        List<BookDto> bookDtos = queryFactory
                .select(Projections.constructor(
                        BookDto.class,
                        book.id,
                        book.title,
                        book.author,
                        book.description,
                        book.publisher,
                        book.publishedDate,
                        book.isbn,
                        book.thumbnailUrl,
                        review.id.countDistinct().coalesce(0L),
                        review.rating.avg().coalesce(0.0),
                        book.createdAt,
                        book.updatedAt
                ))
                .from(book)
                .leftJoin(review).on(review.book.id.eq(book.id))
                .where(
                        keywordPredicate(condition.keyword()),
                        cursorWherePredicate(condition, after))
                .groupBy(
                        book.id,
                        book.title,
                        book.author,
                        book.description,
                        book.publisher,
                        book.publishedDate,
                        book.isbn,
                        book.thumbnailUrl,
                        book.createdAt,
                        book.updatedAt
                )
                .having(
                        cursorHavingPredicate(condition, after, reviewCount, avgRating) // review count / rating 커서
                )
                .orderBy(
                        primaryOrder(condition, reviewCount, avgRating), // 주 정렬
                        book.createdAt.desc() // 타이브레이커 고정
                )
                .limit(size + 1)
                .fetch();

        boolean hasNext = bookDtos.size() > size; // 다음 페이지 존재 여부
        if (hasNext) {
            bookDtos.remove(size); // 하나 더 가져온 건 잘라내기
        }


        return new SliceImpl<>(bookDtos, pageable, hasNext);
    }

    private BooleanExpression keywordPredicate(String keyword) {
        if (hasText(keyword)) { // where like %키워드% 역할
            return book.title.contains(keyword)
                    .or(book.description.contains(keyword))
                    .or(book.author.contains(keyword))
                    .or(book.publisher.contains(keyword));
        }
        return null;
    }

    private Instant parseAfter(String after) {
        return hasText(after) ? Instant.parse(after) : null; // after는 UTC timestamptz라 했으니 ISO-8601 가정
    }

    private OrderSpecifier<?> primaryOrder(BookSearchCondition condition,
                                           NumberExpression<Long> reviewCount,
                                           NumberExpression<Double> avgRating) {
        boolean isDirectionAsc = condition.direction() == SortDirection.ASC;

        return switch (condition.orderBy()) {
            case TITLE -> isDirectionAsc ? book.title.asc() : book.title.desc();
            case PUBLISHED_DATE -> isDirectionAsc ? book.publishedDate.asc() : book.publishedDate.desc();
            case REVIEW_COUNT -> isDirectionAsc ? reviewCount.asc() : reviewCount.desc();
            case RATING -> isDirectionAsc ? avgRating.asc() : avgRating.desc();
        };
    }

    /**
     * book 컬럼 기반 커서(where에 들어감): TITLE / PUBLISHED_DATE
     * createdAt은 타이브레이커(동점 깨기) 겸 after 기준.
     * createdAt 정렬은 항상 DESC로 고정한다고 가정.
     */
    private BooleanExpression cursorWherePredicate(BookSearchCondition condition, Instant after) {
        if (condition.cursor() == null || after == null) return null; // 초기 첫번째 기본 페이지에는 cursor, after 없기떄문에

        boolean isDirectionAsc = condition.direction() == SortDirection.ASC;

        return switch (condition.orderBy()) {
            case TITLE -> isDirectionAsc  // 동점비교가있을수있어서 타이브레이커(서브)로 createdAt 추가하여 비교
                    ? book.title.gt(condition.cursor()).or(book.title.eq(condition.cursor()).and(book.createdAt.lt(after)))
                    : book.title.lt(condition.cursor()).or(book.title.eq(condition.cursor()).and(book.createdAt.lt(after)));

            case PUBLISHED_DATE -> {
                LocalDate cursorDate = LocalDate.parse(condition.cursor()); // "yyyy-MM-dd" 인 Sting을 LocalDate로 형변환
                yield isDirectionAsc
                        ? book.publishedDate.gt(cursorDate).or(book.publishedDate.eq(cursorDate).and(book.createdAt.lt(after)))
                        : book.publishedDate.lt(cursorDate).or(book.publishedDate.eq(cursorDate).and(book.createdAt.lt(after)));
            }

            // 집계 기반 커서 는 having으로 처리
            case REVIEW_COUNT, RATING -> null;
        };
    }

    /**
     * 집계값 커서(having에 들어감): REVIEW_COUNT / RATING
     */
    private BooleanExpression cursorHavingPredicate(BookSearchCondition condition,
                                                    Instant after,
                                                    NumberExpression<Long> reviewCount,
                                                    NumberExpression<Double> avgRating) {
        if (condition.cursor() == null || after == null) return null;

        boolean isDirectionAsc = condition.direction() == SortDirection.ASC;

        return switch (condition.orderBy()) {
            case REVIEW_COUNT -> {
                long cursorCount = Long.parseLong(condition.cursor());
                yield isDirectionAsc
                        ? reviewCount.gt(cursorCount).or(reviewCount.eq(cursorCount).and(book.createdAt.lt(after)))
                        : reviewCount.lt(cursorCount).or(reviewCount.eq(cursorCount).and(book.createdAt.lt(after)));
            }
            case RATING -> {
                double cursorRating = Double.parseDouble(condition.cursor());
                yield isDirectionAsc
                        ? avgRating.gt(cursorRating).or(avgRating.eq(cursorRating).and(book.createdAt.lt(after)))
                        : avgRating.lt(cursorRating).or(avgRating.eq(cursorRating).and(book.createdAt.lt(after)));
            }
            case TITLE, PUBLISHED_DATE -> null;
        };
    }
}
