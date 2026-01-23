package com.deokhugam.domain.poweruser.controller.docs;

import com.deokhugam.domain.poweruser.dto.request.PowerUserSearchCondition;
import com.deokhugam.domain.poweruser.dto.response.CursorPageResponsePowerUserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "파워 유저", description = "파워 유저 관련 API")
public interface PowerUserControllerDocs {
    @Operation(
            summary = "파워 유저 목록 조회",
            description = "기간별 파워 유저 목록을 조회합니다."
    )
    @Parameters({
            @Parameter(
                    name = "period",
                    description = "랭킹 기간",
                    schema = @Schema(
                            implementation = String.class,
                            allowableValues = {"DAILY", "WEEKLY", "MONTHLY", "ALL_TIME"}
                    ),
                    example = "DAILY",
                    required = true
            ),
            @Parameter(
                    name = "direction",
                    in = ParameterIn.QUERY,
                    description = "정렬 방향",
                    schema = @Schema(
                            implementation = String.class,
                            defaultValue = "DESC",
                            allowableValues = {"ASC", "DESC"}
                    ),
                    example = "DESC",
                    required = true
            ),
            @Parameter(name = "cursor", description = "커서 페이지네이션 커서", schema = @Schema(implementation = String.class)),
            @Parameter(name = "after", description = "보조커서(createdAt)", schema = @Schema(implementation = String.class, format = "date-time")),
            @Parameter(name = "limit", description = "페이지 크기", schema = @Schema(implementation = Integer.class, format = "int32", defaultValue = "50"), example = "50")
    })
    @ApiResponse(responseCode = "200", description = "파워 유저 목록 조회 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류 등)")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    ResponseEntity<CursorPageResponsePowerUserDto> getPowerUsers(
            @Parameter(hidden = true)
            @ModelAttribute PowerUserSearchCondition condition
    );
}
