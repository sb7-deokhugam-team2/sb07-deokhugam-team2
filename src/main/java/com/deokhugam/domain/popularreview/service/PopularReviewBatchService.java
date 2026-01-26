package com.deokhugam.domain.popularreview.service;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.popularreview.dto.PeriodRange;
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
        Instant now = Instant.now();

        log.info("[PopularReviewBatch] calculatedDate={}, now={}", calculatedDate, now);

        calculateAndSave(PeriodType.DAILY, calculatedDate, now);
        calculateAndSave(PeriodType.WEEKLY, calculatedDate, now);
        calculateAndSave(PeriodType.MONTHLY, calculatedDate, now);
        calculateAndSave(PeriodType.ALL_TIME, calculatedDate, now);
    }

    private void calculateAndSave(PeriodType periodType, Instant calculatedDate, Instant now) {
        PeriodRange range = PeriodRange.from(periodType, calculatedDate, now);

        log.info("[PopularReviewBatch] period={}, start={}, end={}",
                periodType, range.start(), range.end());

        log.info("[PopularReviewBatch] period={}, calculatedDate={}, now = {}", periodType, calculatedDate, now);

        List<PopularReview> popularReviews = popularReviewRepository.calculatePopularReviews(periodType, calculatedDate, now);
        log.info("[PopularReviewBatch] calculated size={}", popularReviews.size());

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