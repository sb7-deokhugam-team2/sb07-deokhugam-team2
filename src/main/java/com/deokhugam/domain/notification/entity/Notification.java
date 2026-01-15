package com.deokhugam.domain.notification.entity;

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
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseUpdateEntity {
    @Column(name = "content")
    private String content;

    @Column(name = "confirmed", nullable = false)
    private boolean confirmed = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    private Notification(String content, Review review, User user) {
        validateUser(user);
        validateReview(review);
        this.content = content;
        this.review = review;
        this.user = user;
    }

    private static void validateReview(Review review) {
        if (review ==null){
            //Exception
        }
    }

    private static void validateUser(User user) {
        if(user ==null){
            //Exception
        }
    }

    public static Notification create(String content, Review review, User user){
        return new Notification(content, review, user);
    }

    public void confirm(){
        this.confirmed=true;
    }
}
