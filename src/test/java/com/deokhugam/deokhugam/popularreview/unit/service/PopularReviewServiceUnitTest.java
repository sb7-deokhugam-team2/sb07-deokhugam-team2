package com.deokhugam.deokhugam.popularreview.unit.service;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.popularreview.dto.request.PopularReviewSearchCondition;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewDto;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewPageResponseDto;
import com.deokhugam.domain.popularreview.mapper.PopularReviewUrlMapper;
import com.deokhugam.domain.popularreview.repository.PopularReviewRepository;
import com.deokhugam.domain.popularreview.service.PopularReviewService;
import com.deokhugam.domain.popularreview.service.PopularReviewServiceImpl;
import com.deokhugam.domain.review.enums.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PopularReviewServiceUnitTest {
    @Mock
    PopularReviewRepository popularReviewRepository;

    @Mock
    PopularReviewUrlMapper popularReviewUrlMapper;

    @InjectMocks
    PopularReviewServiceImpl popularReviewService;

    @Test
    @DisplayName("인기 리뷰 조회 - repo 조회 후 thumbnailUrl full로 변환해서 반환")
    void getPopularReviews_success() {
        // given
        PopularReviewSearchCondition condition = new PopularReviewSearchCondition(
                PeriodType.DAILY,
                SortDirection.ASC,
                null,
                null,
                10
        );

        PopularReviewDto rawDto = new PopularReviewDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "bookTitle",
                "books/abc.jpg",
                UUID.randomUUID(),
                "nickname",
                "content",
                4.5,
                PeriodType.DAILY,
                Instant.parse("2026-01-24T00:00:00Z"),
                1L,
                12.3,
                3L,
                5L
        );

        PopularReviewPageResponseDto repoPage = new PopularReviewPageResponseDto(
                List.of(rawDto),
                "1",
                null,
                1,
                1L,
                false
        );

        PopularReviewDto mappedDto = new PopularReviewDto(
                rawDto.id(),
                rawDto.reviewId(),
                rawDto.bookId(),
                rawDto.bookTitle(),
                "https://cdn.example.com/books/abc.jpg",
                rawDto.userId(),
                rawDto.userNickname(),
                rawDto.reviewContent(),
                rawDto.reviewRating(),
                rawDto.period(),
                rawDto.createdAt(),
                rawDto.rank(),
                rawDto.score(),
                rawDto.likeCount(),
                rawDto.commentCount()
        );

        when(popularReviewRepository.searchPopularReviews(condition)).thenReturn(repoPage);
        when(popularReviewUrlMapper.withFullThumbnailUrl(repoPage.content())).thenReturn(List.of(mappedDto));

        // when
        PopularReviewPageResponseDto result = popularReviewService.getPopularReviews(condition);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).bookThumbnailUrl()).startsWith("https://");

        verify(popularReviewRepository).searchPopularReviews(condition);
        verify(popularReviewUrlMapper).withFullThumbnailUrl(repoPage.content());


    }
}
