package com.deokhugam.domain.review.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ReviewListDto {
    private final UUID id;
    private final UUID userId;
    private final UUID bookId;
    private final String bookTitle;
    private final String bookThumbnailUrl;
    private final Double rating;
    private final String userNickname;
    private final String content;
    private final Long likedCount;
    private final Long commentCount;
    private final Boolean likedByMe;
    private final Instant createdAt;
    private final Instant updatedAt;

    @QueryProjection
    public ReviewListDto(
            UUID id,
            UUID userId,
            UUID bookId,
            String bookTitle,
            String bookThumbnailUrl,
            Double rating,
            String userNickname,
            String content,
            Long likedCount,
            Long commentCount,
            Boolean likedByMe,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookThumbnailUrl = bookThumbnailUrl;
        this.rating = rating;
        this.userNickname = userNickname;
        this.content = content;
        this.likedCount = likedCount;
        this.commentCount = commentCount;
        this.likedByMe = likedByMe;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
