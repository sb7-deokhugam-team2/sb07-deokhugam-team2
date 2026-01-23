package com.deokhugam.domain.popularreview.service;

import com.deokhugam.domain.popularreview.dto.request.PopularReviewSearchCondition;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewDto;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewPageResponseDto;
import com.deokhugam.domain.popularreview.mapper.PopularReviewUrlMapper;
import com.deokhugam.domain.popularreview.repository.PopularReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PopularReviewServiceImpl implements  PopularReviewService {
    private final PopularReviewRepository popularReviewRepository;
    private final PopularReviewUrlMapper popularReviewUrlMapper;

    @Override
    public PopularReviewPageResponseDto getPopularReviews(PopularReviewSearchCondition condition) {
        PopularReviewPageResponseDto page = popularReviewRepository.searchPopularReviews(condition);
        List<PopularReviewDto> popularReviewDtoList = popularReviewUrlMapper.withFullThumbnailUrl(page.content());

        return new PopularReviewPageResponseDto(
                popularReviewDtoList,
                page.nextCursor(),
                page.nextAfter(),
                page.size(),
                page.totalElements(),
                page.hasNext()
        );
    }
}
