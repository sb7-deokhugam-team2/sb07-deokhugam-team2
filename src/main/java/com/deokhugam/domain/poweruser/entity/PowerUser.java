package com.deokhugam.domain.poweruser.entity;

import com.deokhugam.domain.base.BaseEntity;
import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Getter
@Table(name = "power_users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PowerUser extends BaseEntity {
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

    @Column(name = "review_score_sum", nullable = false)
    private Double reviewScoreSum = 0.0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    private PowerUser(PeriodType periodType,
                      Instant calculatedDate,
                      Long rank,
                      Double score,
                      Long likedCount,
                      Long commentCount,
                      Long reviewScoreSum,
                      User user) {
        this.periodType = periodType;
        this.calculatedDate = calculatedDate;
        this.rank = rank;
        this.score = score;
        this.likedCount = likedCount;
        this.commentCount = commentCount;
        this.reviewScoreSum = reviewScoreSum;
        this.user = user;
    }

    public static PowerUser create(PeriodType periodType,
                                   Instant calculatedDate,
                                   Long rank,
                                   Double score,
                                   Long likedCount,
                                   Long commentCount,
                                   Long reviewScoreSum,
                                   User user) {
        return new PowerUser(periodType,
                calculatedDate,
                rank,
                score,
                likedCount,
                commentCount,
                reviewScoreSum,
                user);
    }
}
