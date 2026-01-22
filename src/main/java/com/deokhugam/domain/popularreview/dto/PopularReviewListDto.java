package com.deokhugam.domain.popularreview.dto;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewDto;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class PopularReviewListDto {
    private final UUID id;
    private final UUID reviewId;
    private final UUID bookId;
    private final String bookTitle;
    private final String bookThumbnailUrl;
    private final UUID userId;
    private final String userNickname;
    private final String reviewContent;
    private final Double reviewRating;
    private final PeriodType period;
    private final Instant createdAt;
    private final Long rank;
    private final Double score;
    private final Long likeCount;
    private final Long commentCount;


    @QueryProjection
    public PopularReviewListDto(
            UUID id,
            UUID reviewId,
            UUID bookId,
            String bookTitle,
            String bookThumbnailUrl,
            UUID userId,
            String userNickname,
            String reviewContent,
            Double reviewRating,
            PeriodType period,
            Instant createdAt,
            Long rank,
            Double score,
            Long likeCount,
            Long commentCount
    ) {
        this.id = id;
        this.reviewId = reviewId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookThumbnailUrl = bookThumbnailUrl;
        this.userId = userId;
        this.userNickname = userNickname;
        this.reviewContent = reviewContent;
        this.reviewRating = reviewRating;
        this.period = period;
        this.createdAt = createdAt;
        this.rank = rank;
        this.score = score;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

    public PopularReviewDto toDto() {
        return new PopularReviewDto(
                id,
                reviewId,
                bookId,
                bookTitle,
                bookThumbnailUrl,
                userId,
                userNickname,
                reviewContent,
                reviewRating,
                period,
                createdAt,
                rank,
                score,
                likeCount,
                commentCount
        );
    }
}
