package com.deokhugam.domain.popularreview.mapper;

import com.deokhugam.domain.book.mapper.BookThumbnailUrlResolver;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PopularReviewUrlMapper {
    private final BookThumbnailUrlResolver thumbnailUrlResolver;

    public PopularReviewDto withFullThumbnailUrl(PopularReviewDto dto) {

        String fullUrl = thumbnailUrlResolver.toFullUrl(dto.bookThumbnailUrl());

        return new PopularReviewDto(
                dto.id(),
                dto.reviewId(),
                dto.bookId(),
                dto.bookTitle(),
                fullUrl,
                dto.userId(),
                dto.userNickname(),
                dto.reviewContent(),
                dto.reviewRating(),
                dto.period(),
                dto.createdAt(),
                dto.rank(),
                dto.score(),
                dto.likeCount(),
                dto.commentCount()
        );
    }

    public List<PopularReviewDto> withFullThumbnailUrl(List<PopularReviewDto> list) {
        return list.stream().map(this::withFullThumbnailUrl).toList();
    }
}

