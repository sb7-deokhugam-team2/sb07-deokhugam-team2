package com.deokhugam.domain.review.mapper;

import com.deokhugam.domain.book.mapper.BookThumbnailUrlResolver;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewUrlMapper {
    private final BookThumbnailUrlResolver thumbnailUrlResolver;

    public ReviewDto withFullThumbnailUrl(ReviewDto dto) {

        String fullUrl = thumbnailUrlResolver.toFullUrl(dto.bookThumbnailUrl());

        return new ReviewDto(
                dto.id(),
                dto.userId(),
                dto.bookId(),
                dto.bookTitle(),
                fullUrl,               // ✅ 여기만 변환
                dto.rating(),
                dto.userNickname(),
                dto.content(),
                dto.likeCount(),
                dto.commentCount(),
                dto.likedByMe(),
                dto.createdAt(),
                dto.updatedAt()
        );
    }

    public List<ReviewDto> withFullThumbnailUrl(List<ReviewDto> list) {
        return list.stream().map(this::withFullThumbnailUrl).toList();
    }
}

