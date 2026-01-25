package com.deokhugam.domain.popularreview.entity;

import com.deokhugam.domain.base.BaseEntity;
import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.review.entity.Review;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Getter
@Table(name = "popular_reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularReview extends BaseEntity {
    @Column(name = "period_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    @Column(name = "calculated_date", nullable = false)
    private Instant calculatedDate;

    @Column(name = "rank", nullable = false)
    private Long rank;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "liked_count", nullable = false)
    private Long likedCount = 0L;

    @Column(name = "comment_count", nullable = false)
    private Long commentCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review;

    private PopularReview(
            PeriodType periodType,
            Instant calculatedDate,
            Long rank,
            Double score,
            Long likedCount,
            Long commentCount,
            Review review) {
        this.periodType = periodType;
        this.calculatedDate = calculatedDate;
        this.rank = rank;
        this.score = score;
        this.likedCount = likedCount;
        this.commentCount = commentCount;
        this.review = review;
    }

    public static PopularReview create(
            PeriodType periodType,
            Instant calculatedDate,
            long rank,
            double score,
            long likedCount,
            long commentCount,
            Review review
    ) {
        return new PopularReview(
                periodType,
                calculatedDate,
                rank,
                score,
                likedCount,
                commentCount,
                review
        );
    }
}
