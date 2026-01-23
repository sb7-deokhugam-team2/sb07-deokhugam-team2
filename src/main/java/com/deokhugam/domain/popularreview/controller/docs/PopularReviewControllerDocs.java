package com.deokhugam.domain.popularreview.controller.docs;

import com.deokhugam.domain.popularreview.dto.request.PopularReviewSearchCondition;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "리뷰 관리", description = "인기 리뷰 관리 API")
public interface PopularReviewControllerDocs {

    @Operation(
            summary = "인기 리뷰 목록 조회",
            description = "주어진 조건에 따라 인기 리뷰 목록을 페이징하여 가져옵니다.",
            operationId = "getPopularReviews"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인기 리뷰 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청으로 요청 파라미터 검증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    ResponseEntity<PopularReviewPageResponseDto> getPopularReviews(
            @Parameter(description = "인기 리뷰 조회 조건(PopularReviewSearchCondition)")
            @ModelAttribute PopularReviewSearchCondition popularReviewSearchCondition
    );
}
