package com.deokhugam.domain.likedreview.controller.docs;

import com.deokhugam.domain.likedreview.dto.response.LikedReviewDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@Tag(name = "리뷰 좋아요", description = "리뷰 좋아요 API")
public interface LikedReviewControllerDocs {
    @Operation(summary = "리뷰 좋아요", description = "리뷰에 좋아요를 추가하거나 취소합니다.")
    @Parameter(
            name = "reviewId",
            description = "리뷰 ID",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Parameter(
            name = "Deokhugam-Request-User-ID",
            description = "요청자 ID",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @ApiResponse(responseCode = "200", description = "리뷰 좋아요 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)")
    @ApiResponse(responseCode = "404", description = "리뷰 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<LikedReviewDto> likeReview(
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
            @PathVariable UUID reviewId);
}
