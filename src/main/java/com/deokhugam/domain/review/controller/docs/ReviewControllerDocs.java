package com.deokhugam.domain.review.controller.docs;

import com.deokhugam.domain.review.dto.request.CursorPageRequest;
import com.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.domain.review.dto.request.ReviewSearchCondition;
import com.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "리뷰 관리", description = "리뷰 관련 API")
public interface ReviewControllerDocs {

    @Operation(summary = "리뷰 목록 조회", description = "검색 조건에 맞는 리뷰 목록을 조회합니다.")
    @Parameters({
            @Parameter(
                    name = "userId",
                    description = "작성자 ID",
                    example = "123e4567-e89b-12d3-a456-426614174000",
                    schema = @Schema(
                            implementation = String.class,
                            format = "uuid"
                    )
            ),
            @Parameter(
                    name = "bookId",
                    description = "도서 ID",
                    example = "123e4567-e89b-12d3-a456-426614174000",
                    schema = @Schema(
                            implementation = String.class,
                            format = "uuid"
                    )
            ),
            @Parameter(
                    name = "keyword",
                    description = "검색 키워드 (작성자 닉네임 | 내용)",
                    example = "홍길동",
                    schema = @Schema(implementation = String.class)
            ),
            @Parameter(
                    name = "orderBy",
                    description = "정렬 기준(createdAt|rating)",
                    example = "createdAt",
                    schema = @Schema(
                            implementation = String.class,
                            defaultValue = "createdAt"
                    )
            ),
            @Parameter(
                    name = "direction",
                    description = "정렬 방향",
                    example = "DESC",
                    schema = @Schema(
                            implementation = String.class,
                            allowableValues = {"ASC", "DESC"},
                            defaultValue = "DESC"
                    )
            ),
            @Parameter(
                    name = "cursor",
                    description = "커서 페이지네이션 커서",
                    schema = @Schema(implementation = String.class)
            ),
            @Parameter(
                    name = "after",
                    description = "보조 커서(createdAt)",
                    schema = @Schema(implementation = String.class, format = "date-time")
            ),
            @Parameter(
                    name = "limit",
                    description = "페이지 크기",
                    example = "50",
                    schema = @Schema(
                            implementation = Integer.class,
                            format = "int32",
                            defaultValue = "50"
                    )
            ),
            @Parameter(
                    name = "requestUserId",
                    description = "요청자 ID",
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
    })
    @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류, 요청자 ID 누락)")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ReviewPageResponseDto> getList(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @Parameter(hidden = true)
            @Valid @ModelAttribute ReviewSearchCondition condition,
            @Parameter(hidden = true)
            @Valid @ModelAttribute CursorPageRequest pageRequest,
            @RequestParam(name = "requestUserId") UUID requestUserId
    );

    @Operation(summary = "리뷰 등록", description = "새로운 리뷰를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "리뷰 등록 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)")
    @ApiResponse(responseCode = "404", description = "도서 정보 없음")
    @ApiResponse(responseCode = "409", description = "이미 작성된 리뷰 존재")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ReviewDto> create(
            @Valid @RequestBody ReviewCreateRequest reviewCreateRequest
    );

    @Operation(summary = "리뷰 상세 정보 조회", description = "리뷰 ID 로 상세 정보를 조회합니다.")
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
    @ApiResponse(responseCode = "200", description = "리뷰 정보 조회 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)")
    @ApiResponse(responseCode = "404", description = "리뷰 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ReviewDto> get(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
            @PathVariable UUID reviewId
    );

    @Operation(summary = "리뷰 논리 삭제", description = "본인이 작성한 리뷰를 논리적으로 삭제합니다.")
    @Parameter(
            name = "Deokhugam-Request-User-ID",
            description = "요청자 ID",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Parameter(
            name = "reviewId",
            description = "리뷰 ID",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)")
    @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음")
    @ApiResponse(responseCode = "404", description = "리뷰 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<Void> softDelete(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @PathVariable UUID reviewId
    );

    @Operation(
            summary = "리뷰 수정",
            description = "본인이 작성한 리뷰를 수정합니다."
    )
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
    @ApiResponse(responseCode = "200", description = "리뷰 수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)")
    @ApiResponse(responseCode = "403", description = "리뷰 수정 권한 없음")
    @ApiResponse(responseCode = "404", description = "리뷰 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ReviewDto> update(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest reviewUpdateRequest
    );

    @Operation(
            summary = "리뷰 물리 삭제",
            description = "본인이 작성한 리뷰를 물리적으로 삭제합니다."
    )
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
    @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)")
    @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음")
    @ApiResponse(responseCode = "404", description = "리뷰 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<Void> hardDelete(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestId,
            @PathVariable UUID reviewId
    );
}
