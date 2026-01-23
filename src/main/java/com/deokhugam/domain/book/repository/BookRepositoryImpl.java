package com.deokhugam.domain.book.repository;

import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.enums.SortCriteria;
import com.deokhugam.domain.book.enums.SortDirection;
import com.deokhugam.domain.popularbook.dto.response.CursorResult;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

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
    private final EntityManager em;

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
                .leftJoin(review).on(
                        review.book.id.eq(book.id),
                        review.isDeleted.isFalse() // NOTE: where에 두지않는건 inner join으로하게되면 book자체가 안나오기떄문
                )
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
    public CursorResult<BookDto> findBooks(BookSearchCondition condition, Pageable pageable) {
        int size = pageable.getPageSize(); // TODO: Pageable 지운걸로 변경
        Instant after = condition.after();

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
                .leftJoin(review).on(
                        review.book.id.eq(book.id),
                        review.isDeleted.isFalse()
                )
                .where(
                        book.isDeleted.isFalse(),
                        keywordPredicate(condition.keyword()),
                        cursorWherePredicate(condition, after)
                )
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
                        condition.direction() == SortDirection.ASC ? book.createdAt.asc() : book.createdAt.desc()
                )
                .limit(size + 1)
                .fetch();

        boolean hasNext = bookDtos.size() > size; // 다음 페이지 존재 여부
        if (hasNext) {
            bookDtos.remove(size); // hasNext 여부를 위해 하나 더 가져온 건 잘라내기
        }

        long total = countTotal(condition);

        return new CursorResult<>(bookDtos, hasNext, total);
    }


    private BooleanExpression keywordPredicate(String keyword) {
        if (hasText(keyword)) { // where like %키워드% 역할
            return book.title.containsIgnoreCase(keyword)
                    .or(book.isbn.containsIgnoreCase(keyword))
                    .or(book.author.containsIgnoreCase(keyword))
                    .or(book.publisher.containsIgnoreCase(keyword));
        }
        return null;
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
     */
    private BooleanExpression cursorWherePredicate(BookSearchCondition condition, Instant after) {
        if (condition.cursor() == null || after == null) return null; // 초기 첫번째 기본 페이지에는 cursor, after 없기떄문에

        boolean isDirectionAsc = condition.direction() == SortDirection.ASC;

        return switch (condition.orderBy()) {
            case TITLE -> isDirectionAsc  // 동점비교가있을수있어서 타이브레이커(서브)로 createdAt 추가하여 비교
                    ? book.title.gt(condition.cursor()).or(book.title.eq(condition.cursor()).and(book.createdAt.gt(after)))
                    : book.title.lt(condition.cursor()).or(book.title.eq(condition.cursor()).and(book.createdAt.lt(after)));

            case PUBLISHED_DATE -> {
                LocalDate cursorDate = LocalDate.parse(condition.cursor()); // "yyyy-MM-dd" 인 Sting을 LocalDate로 형변환
                yield isDirectionAsc
                        ? book.publishedDate.gt(cursorDate).or(book.publishedDate.eq(cursorDate).and(book.createdAt.gt(after)))
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
                        ? reviewCount.gt(cursorCount).or(reviewCount.eq(cursorCount).and(book.createdAt.gt(after)))
                        : reviewCount.lt(cursorCount).or(reviewCount.eq(cursorCount).and(book.createdAt.lt(after)));
            }
            case RATING -> {
                double cursorRating = Double.parseDouble(condition.cursor());
                yield isDirectionAsc
                        ? avgRating.gt(cursorRating).or(avgRating.eq(cursorRating).and(book.createdAt.gt(after)))
                        : avgRating.lt(cursorRating).or(avgRating.eq(cursorRating).and(book.createdAt.lt(after)));
            }
            case TITLE, PUBLISHED_DATE -> null;
        };
    }


    @Override
    public long countTotal(BookSearchCondition condition) {
        // NOTE: fetch().size()로 사용시 네트워크 + 메모리 + GC 과부하로 subquery 사용을 위해 native query로 적용
        String keyword = condition.keyword();
        String cursor = condition.cursor();
        Instant after = condition.after();

        boolean hasCursor = hasCursor(cursor, after);

        String aggregateExpression = resolveAggregateExpression(condition.orderBy());
        String keywordWhere = buildKeywordWhere();
        String havingClause = buildHavingClause(condition, hasCursor, aggregateExpression);

        String sql = buildTotalCountSql(keywordWhere, havingClause);

        Query query = em.createNativeQuery(sql);
        bindParameters(query, condition, keyword, hasCursor, aggregateExpression, cursor, after);

        Number result = (Number) query.getSingleResult();
        return result.longValue();
    }

    private boolean hasCursor(String cursor, Instant after) {
        return cursor != null && after != null;
    }

    private String resolveAggregateExpression(SortCriteria orderBy) {
        return switch (orderBy) {
            case REVIEW_COUNT -> "COUNT(DISTINCT r.id)";
            case RATING -> "COALESCE(AVG(r.rating), 0)";
            case TITLE, PUBLISHED_DATE -> null; // 현재 구현: having 불필요
        };
    }

    private String buildKeywordWhere() {
        return """
            AND (
                :keyword IS NULL OR :keyword = '' OR
                LOWER(b.title) LIKE CONCAT('%%', LOWER(:keyword), '%%') OR
                LOWER(b.isbn) LIKE CONCAT('%%', LOWER(:keyword), '%%') OR
                LOWER(b.author) LIKE CONCAT('%%', LOWER(:keyword), '%%') OR
                LOWER(b.publisher) LIKE CONCAT('%%', LOWER(:keyword), '%%')
            )
        """;
    }

    private String buildHavingClause(BookSearchCondition condition,
                                     boolean hasCursor,
                                     String aggregateExpression) {
        // 집계 커서(REVIEW_COUNT / RATING)만 having을 만든다
        if (!hasCursor) return "";
        if (aggregateExpression == null) return ""; // TITLE/PUBLISHED_DATE 케이스

        SortCriteria orderBy = condition.orderBy();
        if (orderBy != SortCriteria.REVIEW_COUNT && orderBy != SortCriteria.RATING) return "";

        String operator = (condition.direction() == SortDirection.ASC) ? ">" : "<";

        return """
            HAVING (
                (%s %s :cursorVal)
                OR (%s = :cursorVal AND b.created_at %s :after)
            )
        """.formatted(aggregateExpression, operator, aggregateExpression, operator);
    }

    private String buildTotalCountSql(String keywordWhere, String havingClause) {
        return """
            SELECT COUNT(*) AS total
            FROM (
                SELECT b.id
                FROM books b
                LEFT JOIN reviews r
                       ON r.book_id = b.id
                      AND r.is_deleted = false
                WHERE b.is_deleted = false
                %s
                GROUP BY
                  b.id, b.title, b.author, b.description, b.publisher,
                  b.published_date, b.isbn, b.thumbnail_url,
                  b.created_at, b.updated_at
                %s
            ) x
        """.formatted(keywordWhere, havingClause);
    }

    private void bindParameters(Query query,
                                BookSearchCondition condition,
                                String keyword,
                                boolean hasCursor,
                                String aggregateExpression,
                                String cursor,
                                Instant after) {
        query.setParameter("keyword", keyword);

        // having이 붙는 케이스에서만 cursorVal/after를 바인딩
        if (!hasCursor) return;
        if (aggregateExpression == null) return;

        if (condition.orderBy() == SortCriteria.REVIEW_COUNT) {
            query.setParameter("cursorVal", Long.parseLong(cursor));
        } else if (condition.orderBy() == SortCriteria.RATING) {
            query.setParameter("cursorVal", Double.parseDouble(cursor));
        } else {
            return;
        }

        query.setParameter("after", after);
    }
}
