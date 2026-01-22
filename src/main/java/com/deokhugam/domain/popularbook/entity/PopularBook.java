package com.deokhugam.domain.popularbook.entity;

import com.deokhugam.domain.base.BaseEntity;
import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.popularbook.exception.PopularBookException;
import com.deokhugam.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.Map;

@Entity
@Getter
@Table(name = "popular_books")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularBook extends BaseEntity {
    @Column(name = "period_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    @Column(name = "calculated_date", nullable = false)
    private Instant calculatedDate;

    @Column(name = "rank", nullable = false)
    private Long rank;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "rating", nullable = false)
    private Double rating;

    @Column(name = "review_count", nullable = false)
    private Long reviewCount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Book book;

    public PopularBook(PeriodType periodType, Instant calculatedDate, Long rank, Double score, Double rating, Long reviewCount, Book book) {
        if (periodType == null)
            throw new PopularBookException(ErrorCode.POPULAR_BOOK_INVALID_STATE, Map.of("field", "periodType", "reason", "null일 수 없습니다."));
        if (calculatedDate == null)
            throw new PopularBookException(ErrorCode.POPULAR_BOOK_INVALID_STATE, Map.of("field", "calculatedDate", "reason", "null일 수 없습니다."));
        if (rank == null)
            throw new PopularBookException(ErrorCode.POPULAR_BOOK_INVALID_STATE, Map.of("field", "rank", "reason", "null일 수 없습니다."));
        if (score == null)
            throw new PopularBookException(ErrorCode.POPULAR_BOOK_INVALID_STATE, Map.of("field", "score", "reason", "null일 수 없습니다."));
        if (rating == null)
            throw new PopularBookException(ErrorCode.POPULAR_BOOK_INVALID_STATE, Map.of("field", "rating", "reason", "null일 수 없습니다."));
        if (reviewCount == null)
            throw new PopularBookException(ErrorCode.POPULAR_BOOK_INVALID_STATE, Map.of("field", "reviewCount", "reason", "null일 수 없습니다."));
        if (book == null)
            throw new PopularBookException(ErrorCode.POPULAR_BOOK_INVALID_STATE, Map.of("field", "book", "reason", "null일 수 없습니다."));

        this.periodType = periodType;
        this.calculatedDate = calculatedDate;
        this.rank = rank;
        this.score = score;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.book = book;
    }

    public static PopularBook create(PeriodType periodType, Instant calculatedDate, Long rank, Double score, Double rating, Long reviewCount, Book book) {
        return new PopularBook(
                periodType,
                calculatedDate,
                rank,
                score,
                rating,
                reviewCount,
                book
        );
    }
}
