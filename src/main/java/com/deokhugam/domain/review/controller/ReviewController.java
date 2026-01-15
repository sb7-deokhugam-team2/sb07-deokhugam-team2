package com.deokhugam.domain.review.controller;

import com.deokhugam.domain.review.dto.request.*;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;
import com.deokhugam.domain.review.enums.ReviewOrderBy;
import com.deokhugam.domain.review.enums.SortDirection;
import com.deokhugam.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ReviewPageResponseDto> getList(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @Valid ReviewSearchCondition condition,
            @Valid CursorPageRequest pageRequest,
            @RequestParam(name = "requestUserId") UUID requestUserId
    ) {
        // TODO: Custom예외 적용 필요
        if (!requestId.equals(requestUserId)) {
            throw new IllegalArgumentException("요청자의 ID가 다릅니다.");
        }

        return ResponseEntity.ok(reviewService.searchReviews(condition, pageRequest, requestId));
    }

    @PostMapping
    public ResponseEntity<ReviewDto> create(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @Valid @RequestBody ReviewCreateRequest reviewCreateRequest
    ) {
        return null;
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> get(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @PathVariable UUID reviewId
    ) {
        return null;

    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> softDelete(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @PathVariable UUID reviewId
    ) {
        return null;
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> update(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest reviewUpdateRequest
    ) {
        return null;
    }

    @DeleteMapping("/{reviewId}/hard")
    public ResponseEntity<Void> hardDelete(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @PathVariable UUID reviewId
    ) {
        return null;
    }
}
