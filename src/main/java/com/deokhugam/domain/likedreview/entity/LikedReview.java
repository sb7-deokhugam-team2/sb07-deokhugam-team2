package com.deokhugam.domain.likedreview.entity;

import com.deokhugam.domain.base.BaseUpdateEntity;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "liked_reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikedReview extends BaseUpdateEntity {
    @Column(name = "liked", nullable = false)
    private boolean liked = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    private LikedReview(Review review, User user) {
        this.review = review;
        this.user = user;
    }

    public static LikedReview create(Review review, User user) {
        return new LikedReview(review, user);
    }

    public boolean toggle() {
        this.liked = !this.liked;
        return this.liked;
    }

    public void like() {
        this.liked = true;
    }

    public void unlike() {
        this.liked = false;
    }
}
