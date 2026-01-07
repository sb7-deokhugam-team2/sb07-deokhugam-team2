package com.deokhugam.domain.review.entity;

import com.deokhugam.domain.base.BaseDeletableEntity;
import com.deokhugam.domain.book.entity.Book;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

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

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    // TODO: user 연관관계 필드 설정 필요
}
