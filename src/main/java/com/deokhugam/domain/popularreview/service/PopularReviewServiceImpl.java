package com.deokhugam.domain.popularreview.service;

import com.deokhugam.domain.popularreview.dto.request.PopularReviewSearchCondition;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewPageResponseDto;
import org.springframework.stereotype.Service;

@Service
public class PopularReviewServiceImpl implements  PopularReviewService {
    @Override
    public PopularReviewPageResponseDto getPopularReviews(PopularReviewSearchCondition condition) {
        return null;
    }
}
