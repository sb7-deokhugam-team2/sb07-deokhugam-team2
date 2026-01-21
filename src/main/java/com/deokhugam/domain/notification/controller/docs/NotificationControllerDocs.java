package com.deokhugam.domain.notification.controller.docs;

import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.domain.notification.dto.response.CursorPageResponseNotificationDto;
import com.deokhugam.domain.notification.dto.response.NotificationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "알림 관리", description = "알림 관련 API")
public interface NotificationControllerDocs {

    @Operation(
            summary = "알림 읽음 상태 업데이트",
            description = "특정 알림의 읽음 상태를 업데이트합니다."
    )
    @Parameter(name = "notificationId", description = "알림 ID", example = "123e4567-e89b-12d3-a4560426614174000")
    @Parameter(name = "Deokhugam-Request-User-Id", description = "요청자 ID", example = "123e4567-e89b-12d3-a4560426614174000")
    @ApiResponse(responseCode = "200", description = "알림 상태 업데이트 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, 요청자 ID 누락)")
    @ApiResponse(responseCode = "403", description = "알림 수정 권한 없음")
    @ApiResponse(responseCode = "404", description = "알림 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<NotificationDto> readNotification(
            @PathVariable UUID notificationId,
            @RequestHeader("Deokhugam-Request-User-Id") UUID userId,
            @RequestBody NotificationUpdateRequest notificationUpdateRequest
    );

    @Operation(
            summary = "모든 알림 읽음 처리",
            description = "사용자의 모든 알림을 읽음 상태로 처리합니다."
    )
    @Parameter(name = "Deokhugam-Request-User-ID", description = "사용자 ID", example = "123e4567-e89b-12d3-a4560426614174000")
    @ApiResponse(responseCode = "204", description = "알림 읽음 처리 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (사용자 ID 누락)")
    @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<Void> readAll(
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId
    );

    @Operation(
            summary = "알림 목록 조회",
            description = "사용자의 알림 목록을 조회합니다."
    )
    @Parameters({
            @Parameter(
                    name = "userId",
                    description = "사용자 ID",
                    example = "123e4567-e89b-12d3-a4560426614174000",
                    required = true,
                    schema = @Schema(
                            implementation = String.class,
                            format = "uuid"
                    )
            ),
            @Parameter(
                    name = "direction",
                    description = "정렬 방향",
                    schema = @Schema(
                            implementation = String.class,
                            allowableValues = {"ASC", "DESC"},
                            defaultValue = "DESC"
                    ),
                    example = "DESC"
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
                    schema = @Schema(
                            implementation = Integer.class,
                            defaultValue = "20",
                            example = "20"
                    )
            )
    })
    @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 방향 오류, 페이지네이션 파라미터 오류, 사용자 ID 누락)")
    @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<CursorPageResponseNotificationDto> getNotification(
            @Parameter(hidden = true)
            @ModelAttribute NotificationSearchCondition condition
    );
}
