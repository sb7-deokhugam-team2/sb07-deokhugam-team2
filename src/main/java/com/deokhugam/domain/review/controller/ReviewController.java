package com.deokhugam.domain.review.controller;

import com.deokhugam.domain.review.controller.docs.ReviewControllerDocs;
import com.deokhugam.domain.review.dto.request.*;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;
import com.deokhugam.domain.review.exception.ReviewNotEqualException;
import com.deokhugam.domain.review.service.ReviewService;
import com.deokhugam.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController implements ReviewControllerDocs {
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ReviewPageResponseDto> getList(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @Valid @ModelAttribute ReviewSearchCondition condition,
            @Valid @ModelAttribute CursorPageRequest pageRequest,
            @RequestParam(name = "requestUserId") UUID requestUserId
    ) {
        if (!requestId.equals(requestUserId)) {
            throw new ReviewNotEqualException(ErrorCode.REVIEW_ID_NOT_EQUAL);
        }

        return ResponseEntity.ok(reviewService.searchReviews(condition, pageRequest, requestId));
    }

    @PostMapping
    public ResponseEntity<ReviewDto> create(
            @Valid @RequestBody ReviewCreateRequest reviewCreateRequest
    ) {
        ReviewDto createdReview = reviewService.createReview(reviewCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> get(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
            @PathVariable UUID reviewId
    ) {
        return ResponseEntity.ok(reviewService.getReview(requestUserId, reviewId));

    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> softDelete(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @PathVariable UUID reviewId
    ) {
        reviewService.softDeleteReview(reviewId, requestId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> update(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest reviewUpdateRequest
    ) {
        ReviewDto updatedReview = reviewService.updateReview(reviewUpdateRequest, reviewId, requestId);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{reviewId}/hard")
    public ResponseEntity<Void> hardDelete(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @PathVariable UUID reviewId
    ) {
        reviewService.hardDeleteReview(reviewId, requestId);
        return ResponseEntity.noContent().build();
    }
}
