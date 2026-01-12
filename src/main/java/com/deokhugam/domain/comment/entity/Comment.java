package com.deokhugam.domain.comment.entity;

import com.deokhugam.domain.base.BaseDeletableEntity;
import com.deokhugam.domain.comment.exception.CommentContentException;
import com.deokhugam.domain.comment.exception.CommentReviewNullException;
import com.deokhugam.domain.comment.exception.CommentUserNullException;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseDeletableEntity {
    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review;

    private Comment(String content, User user, Review review) {
        this.content = content;
        this.user = user;
        this.review = review;
    }

    public static Comment create(String content, User user, Review review) {

        validateReview(review);
        validateUser(user);
        validateContent(content);

        return new Comment(content, user, review);
    }

    public void updateContent(String newContent) {
        validateContent(newContent);
        this.content = newContent;
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new CommentContentException(ErrorCode.COMMENT_CONTENT_VALIDATION);
        }
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new CommentUserNullException(ErrorCode.COMMENT_USER_NULL);
        }
    }

    private static void validateReview(Review review) {
        if (review == null) {
            throw new CommentReviewNullException(ErrorCode.COMMENT_REVIEW_NULL);
        }
    }
}
