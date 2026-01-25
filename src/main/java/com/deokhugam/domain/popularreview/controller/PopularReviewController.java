package com.deokhugam.domain.popularreview.controller;

import com.deokhugam.domain.popularreview.controller.docs.PopularReviewControllerDocs;
import com.deokhugam.domain.popularreview.dto.request.PopularReviewSearchCondition;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewPageResponseDto;
import com.deokhugam.domain.popularreview.service.PopularReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class PopularReviewController implements PopularReviewControllerDocs {
    private final PopularReviewService popularReviewService;

    @Override
    @GetMapping("/popular")
    public ResponseEntity<PopularReviewPageResponseDto> getPopularReviews(
            @Valid PopularReviewSearchCondition popularReviewSearchCondition
    ) {
        log.debug("getPopularReviews 시작 - 요청 파라미터: {}", popularReviewSearchCondition);

        PopularReviewPageResponseDto response = popularReviewService.getPopularReviews(popularReviewSearchCondition);

        log.info("getPopularReviews 완료 - 응답 내용: {}", response);
        return ResponseEntity.ok(response);
    }
}
