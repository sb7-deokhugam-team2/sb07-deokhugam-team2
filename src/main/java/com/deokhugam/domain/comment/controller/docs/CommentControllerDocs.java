package com.deokhugam.domain.comment.controller.docs;

import com.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.domain.comment.dto.request.CommentSearchCondition;
import com.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.dto.response.CursorPageResponseCommentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "댓글 관리", description = "댓글 관련 API")
public interface CommentControllerDocs {
    @Operation(
            summary = "리뷰 댓글 목록 조회",
            description = "특정 리뷰에 달린 댓글 목록을 시간순으로 조회합니다."
    )
    @Parameters({
            @Parameter(
                    name = "reviewId",
                    description = "리뷰 ID",
                    schema = @Schema(
                            implementation = String.class ,
                            format = "uuid"
                    ),
                    example = "123e4567-e89b-12d3-a456-426614174000",
                    required = true
            ),
            @Parameter(
                    name = "direction",
                    in = ParameterIn.QUERY,
                    description = "정렬 방향",
                    schema = @Schema(
                            implementation = String.class ,
                            defaultValue = "DESC",
                            allowableValues = {"ASC", "DESC"}
                    ),
                    example = "DESC"
            ),
            @Parameter(name = "cursor", description = "커서 페이지네이션 커서", schema = @Schema(implementation = String.class)),
            @Parameter(name = "after", description = "보조커서(createdAt)", schema = @Schema(implementation = String.class , format = "date-time")),
            @Parameter(name = "limit", description = "페이지 크기", schema = @Schema(implementation = Integer.class, format = "int32", defaultValue = "50"), example = "50")
    })
    @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 방향 오류, 페이지네이션 파라미터 오류, 리뷰 ID 누락)")
    @ApiResponse(responseCode = "404", description = "리뷰 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<CursorPageResponseCommentDto> getComments(
            @Parameter(hidden = true)
            @Valid @ModelAttribute CommentSearchCondition commentSearchCondition
    );


    @Operation(
            summary = "댓글 등록",
            description = "새로운 댓글을 등록합니다."
    )
    @ApiResponse(responseCode = "201", description = "댓글 등록 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)")
    @ApiResponse(responseCode = "404", description = "리뷰 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<CommentDto> createComment(
            @Valid @RequestBody CommentCreateRequest commentCreateRequest
    );

    @Operation(
            summary = "댓글 상세 정보 조회",
            description = "특정 댓글의 상세 정보를 조회합니다."
    )
    @Parameter(
            name = "commentId",
            description = "댓글 ID",
            schema = @Schema(implementation = UUID.class, format = "uuid"),
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true
    )
    @ApiResponse(responseCode = "200", description = "댓글 조회 성공")
    @ApiResponse(responseCode = "404", description = "댓글 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<CommentDto> getComment(
            @Parameter(hidden = true)
            @PathVariable UUID commentId
    );

    @Operation(
            summary = "댓글 논리 삭제",
            description = "본인이 작성한 댓글을 논리적으로 삭제합니다."
    )
    @Parameter(
            name = "commentId",
            in = ParameterIn.PATH,
            description = "댓글 ID",
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true,
            schema = @Schema(implementation = String.class, format = "uuid")
    )
    @Parameter(
            name = "Deokhugam-Request-User-Id",
            description = "요청자 ID",
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true
    )
    @ApiResponse(responseCode = "204", description = "댓글 삭제 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)")
    @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음")
    @ApiResponse(responseCode = "404", description = "댓글 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<Void> logicalDelete(
            @Parameter(hidden = true)
            @PathVariable UUID commentId,
            @RequestHeader(value = "Deokhugam-Request-User-Id") UUID userId
    );

    @Operation(
            summary = "댓글 수정",
            description = "본인이 작성한 댓글을 수정합니다."
    )
    @Parameter(
            name = "commentId",
            in = ParameterIn.PATH,
            description = "댓글 ID",
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true,
            schema = @Schema(implementation = String.class, format = "uuid")
    )
    @Parameter(
            name = "Deokhugam-Request-User-Id",
            description = "요청자 ID",
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true
    )
    @ApiResponse(responseCode = "200", description = "댓글 수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, 요청자 ID 누락)")
    @ApiResponse(responseCode = "403", description = "댓글 수정 권한 없음")
    @ApiResponse(responseCode = "404", description = "댓글 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<CommentDto> updateComment(
            @Parameter(hidden = true)
            @PathVariable UUID commentId,
            @RequestHeader(value = "Deokhugam-Request-User-Id") UUID userId,
            @Valid @RequestBody CommentUpdateRequest commentUpdateRequest
    );

    @Operation(
            summary = "댓글 물리 삭제",
            description = "본인이 작성한 댓글을 물리적으로 삭제합니다."
    )
    @Parameter(
            name = "commentId",
            in = ParameterIn.PATH,
            description = "댓글 ID",
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true,
            schema = @Schema(implementation = String.class, format = "uuid")
    )
    @Parameter(
            name = "Deokhugam-Request-User-Id",
            description = "요청자 ID",
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true
    )
    @ApiResponse(responseCode = "204", description = "댓글 삭제 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)")
    @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음")
    @ApiResponse(responseCode = "404", description = "댓글 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<Void> physicalDelete(
            @Parameter(hidden = true)
            @PathVariable UUID commentId,
            @RequestHeader(value = "Deokhugam-Request-User-Id") UUID userId
    );
}
