package com.deokhugam.domain.popularbook.entity;

import com.deokhugam.domain.base.BaseEntity;
import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.entity.Book;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

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
}
