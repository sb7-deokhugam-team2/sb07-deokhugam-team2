package com.deokhugam.domain.popularreview.repository;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.popularreview.dto.request.PopularReviewSearchCondition;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewPageResponseDto;
import com.deokhugam.domain.popularreview.entity.PopularReview;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PopularReviewRepositoryCustom {
    List<PopularReview> calculatePopularReviews(PeriodType periodType, Instant calculatedDate);
    PopularReviewPageResponseDto searchPopularReviews(PopularReviewSearchCondition condition, UUID requestUserId);
}
