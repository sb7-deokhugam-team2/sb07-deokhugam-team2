package com.deokhugam.domain.popularreview.service;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.popularreview.entity.PopularReview;
import com.deokhugam.domain.popularreview.repository.PopularReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularReviewBatchService {
    private final PopularReviewRepository popularReviewRepository;

    @Transactional
    public void calculateAndSaveAllPeriods() {
        Instant calculatedDate = getTodayStartKst();

        calculateAndSave(PeriodType.DAILY, calculatedDate);
        calculateAndSave(PeriodType.WEEKLY, calculatedDate);
        calculateAndSave(PeriodType.MONTHLY, calculatedDate);
        calculateAndSave(PeriodType.ALL_TIME, calculatedDate);
    }

    private void calculateAndSave(PeriodType periodType, Instant calculatedDate) {
        log.info("[PopularReviewBatch] period={}, calculatedDate={}", periodType, calculatedDate);

        List<PopularReview> popularReviews = popularReviewRepository.calculatePopularReviews(periodType, calculatedDate);

        popularReviewRepository.deleteByPeriodTypeAndCalculatedDate(periodType, calculatedDate);

        popularReviewRepository.saveAll(popularReviews);

        log.info("[PopularReviewBatch] saved: {}", popularReviews.size());
    }

    private Instant getTodayStartKst() {
        return LocalDate.now(ZoneId.of("Asia/Seoul"))
                .atStartOfDay(ZoneId.of("Asia/Seoul"))
                .toInstant();
    }
}