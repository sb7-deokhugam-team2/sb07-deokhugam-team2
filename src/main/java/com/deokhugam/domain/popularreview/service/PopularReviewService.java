package com.deokhugam.domain.popularreview.service;

import com.deokhugam.domain.popularreview.dto.request.PopularReviewSearchCondition;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewPageResponseDto;

public interface PopularReviewService {
    PopularReviewPageResponseDto getPopularReviews(PopularReviewSearchCondition condition);
}
