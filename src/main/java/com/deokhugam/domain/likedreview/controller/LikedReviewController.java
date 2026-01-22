package com.deokhugam.domain.likedreview.controller;

import com.deokhugam.domain.likedreview.controller.docs.LikedReviewControllerDocs;
import com.deokhugam.domain.likedreview.dto.response.LikedReviewDto;
import com.deokhugam.domain.likedreview.service.LikedReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LikedReviewController implements LikedReviewControllerDocs {
    private final LikedReviewService likedReviewService;

    @PostMapping("/api/reviews/{reviewId}/like")
    public ResponseEntity<LikedReviewDto> likeReview(
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
            @PathVariable UUID reviewId) {
        return ResponseEntity.ok(likedReviewService.toggleLike(reviewId, userId));
    }
}
