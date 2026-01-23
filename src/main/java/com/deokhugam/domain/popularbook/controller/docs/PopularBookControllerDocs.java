package com.deokhugam.domain.popularbook.controller.docs;

import com.deokhugam.domain.popularbook.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.popularbook.dto.response.CursorPageResponsePopularBookDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "도서 관리", description = "도서 관련 API")
public interface PopularBookControllerDocs {

    @Operation(
            summary = "인기 도서 목록 조회",
            description = "기간별 인기 도서 목록을 조회합니다.",
            operationId = "getPopularBooks"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인기 도서 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류 등)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<CursorPageResponsePopularBookDto> getPopularBooks(
            @Parameter(description = "인기 도서 조회 조건 (period, direction, cursor, after, limit)")
            @ModelAttribute PopularBookSearchCondition condition
    );
}