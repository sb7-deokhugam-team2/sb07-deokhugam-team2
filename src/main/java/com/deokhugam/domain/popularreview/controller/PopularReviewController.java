package com.deokhugam.domain.popularreview.controller;

import com.deokhugam.domain.popularreview.dto.response.PopularReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class PopularReviewController {
    @GetMapping("/api/reviews/popular")
    public ResponseEntity<PopularReviewDto> getPopularReviews() {
        return null;
    }
}
