package com.deokhugam.domain.review.mapper;

import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public ReviewDto toReviewDto(Review review, long commentCount, boolean likedByMe) {
        return new ReviewDto(
                review.getId(),
                review.getUser().getId(),
                review.getBook().getId(),
                review.getBook().getTitle(),
                review.getBook().getThumbnailUrl(),
                review.getRating(),
                review.getUser().getNickname(),
                review.getContent(),
                review.getLikedCount(),
                commentCount,
                likedByMe,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
