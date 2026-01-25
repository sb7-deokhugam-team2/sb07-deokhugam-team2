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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
@Slf4j
public class ReviewController implements ReviewControllerDocs {
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ReviewPageResponseDto> getList(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @Valid @ModelAttribute ReviewSearchCondition condition,
            @Valid @ModelAttribute CursorPageRequest pageRequest,
            @RequestParam(name = "requestUserId") UUID requestUserId
    ) {
        log.debug("리뷰 목록 조회 요청 시작 - RequestId: {}, Condition: {}, PageRequest: {}",
                requestId, condition, pageRequest);

        if (!requestId.equals(requestUserId)) {
            log.warn("리뷰 목록 조회 실패 - RequestId와 RequestUserId가 다름. RequestId: {}, RequestUserId: {}", requestId, requestUserId);
            throw new ReviewNotEqualException(ErrorCode.REVIEW_ID_NOT_EQUAL);
        }

        ReviewPageResponseDto response = reviewService.searchReviews(condition, pageRequest, requestId);

        log.info("리뷰 목록 조회 처리 완료 - RequestId: {}, ResultSize: {}", requestId, response.content().size());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReviewDto> create(
            @Valid @RequestBody ReviewCreateRequest reviewCreateRequest
    ) {
        log.debug("리뷰 생성 요청 시작 - UserId: {}, BookId: {}, Content: {}",
                reviewCreateRequest.userId(), reviewCreateRequest.bookId(), reviewCreateRequest.content());

        ReviewDto createdReview = reviewService.createReview(reviewCreateRequest);

        log.info("리뷰 생성 완료 - UserId: {}, BookId: {}", reviewCreateRequest.userId(), reviewCreateRequest.bookId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> get(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
            @PathVariable UUID reviewId
    ) {
        log.debug("리뷰 단건 조회 요청 시작 - ReviewId: {}, UserId: {}", reviewId, requestUserId);

        ReviewDto review = reviewService.getReview(requestUserId, reviewId);

        log.info("리뷰 단건 조회 처리 완료 - ReviewId: {}, UserId: {}", reviewId, requestUserId);

        return ResponseEntity.ok(review);

    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> softDelete(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @PathVariable UUID reviewId
    ) {
        log.debug("리뷰 논리 삭제 요청 시작 - ReviewId: {}, UserId: {}", reviewId, requestId);

        reviewService.softDeleteReview(reviewId, requestId);

        log.info("리뷰 논리 삭제 처리 완료 - ReviewId: {}, UserId: {}", reviewId, requestId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> update(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest reviewUpdateRequest
    ) {
        log.debug("리뷰 수정 요청 시작 - ReviewId: {}, UserId: {}, New Content: {}",
                reviewId, requestId, reviewUpdateRequest.content());

        ReviewDto updatedReview = reviewService.updateReview(reviewUpdateRequest, requestId, reviewId);

        log.info("리뷰 수정 처리 완료 - ReviewId: {}, UserId: {}", requestId, reviewId);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{reviewId}/hard")
    public ResponseEntity<Void> hardDelete(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @PathVariable UUID reviewId
    ) {
        log.debug("리뷰 물리 삭제 요청 시작 - ReviewId: {}, UserId: {}", reviewId, requestId);

        reviewService.hardDeleteReview(reviewId, requestId);

        log.info("리뷰 물리 삭제 처리 완료 - ReviewId: {}, UserId: {}", reviewId, requestId);
        return ResponseEntity.noContent().build();
    }
}
