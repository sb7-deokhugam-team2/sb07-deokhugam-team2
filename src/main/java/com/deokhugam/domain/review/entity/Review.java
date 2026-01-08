package com.deokhugam.domain.review.entity;

import com.deokhugam.domain.base.BaseDeletableEntity;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class Review extends BaseDeletableEntity {
    @Column(name = "rating", nullable = false)
    private Double rating = 0.0;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "liked_count", nullable = false)
    private Long likedCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
}
