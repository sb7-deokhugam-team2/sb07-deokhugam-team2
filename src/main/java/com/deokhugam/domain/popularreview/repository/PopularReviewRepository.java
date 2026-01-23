package com.deokhugam.domain.popularreview.repository;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.popularreview.entity.PopularReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID>, PopularReviewRepositoryCustom {
    void deleteByPeriodTypeAndCalculatedDate(PeriodType periodType, Instant calculatedDate);
}
