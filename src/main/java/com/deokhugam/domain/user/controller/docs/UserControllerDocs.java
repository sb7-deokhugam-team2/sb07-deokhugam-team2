package com.deokhugam.domain.user.controller.docs;

import com.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.domain.user.dto.response.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "사용자 관리", description = "사용자 관련 API")
public interface UserControllerDocs {

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 등록합니다."
    )
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)")
    @ApiResponse(responseCode = "409", description = "이메일 중복")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<UserDto> register(
            @Valid @RequestBody UserRegisterRequest userRegisterRequest
    );

    @Operation(
            summary = "로그인",
            description = "사용자 로그인을 처리합니다."
    )
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)")
    @ApiResponse(responseCode = "401", description = "로그인 실패 (이메일 또는 비밀번호 불일치)")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<UserDto> login(
            @Valid @RequestBody UserLoginRequest userLoginRequest
    );

    @Operation(
            summary = "사용자 정보 조회",
            description = "사용자 ID로 상세 정보를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공")
    @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    @Parameter(
            name = "userId",
            description = "사용자 ID",
            in = ParameterIn.PATH,
            schema = @Schema(
                    implementation = String.class,
                    format = "uuid"
            ),
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true
    )
    public ResponseEntity<UserDto> findUser(
            @Parameter(hidden = true)
            @PathVariable UUID userId
    );

    @Operation(
            summary = "사용자 논리 삭제",
            description = "사용자를 논리적으로 삭제합니다."
    )
    @ApiResponse(responseCode = "204", description = "사용자 삭제 성공")
    @ApiResponse(responseCode = "403", description = "사용자 삭제 권한 없음")
    @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    @Parameter(name = "userId", description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    public ResponseEntity<Void> logicalDelete(
            @PathVariable UUID userId
    );

    @Operation(
            summary = "사용자 정보 수정",
            description = "사용자의 닉네임을 수정합니다."
    )
    @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)")
    @ApiResponse(responseCode = "403", description = "사용자 정보 수정 권한 없음")
    @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    @Parameter(
            name = "userId",
            in = ParameterIn.PATH,
            description = "사용자 ID",
            schema = @Schema(
                    implementation = String.class,
                    format = "uuid"
            ),
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true
    )
    public ResponseEntity<UserDto> updateUser(
            @Parameter(hidden = true)
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest
    );

    @Operation(
            summary = "사용자 물리 삭제",
            description = "사용자를 물리적으로 삭제합니다."
    )
    @ApiResponse(responseCode = "204", description = "사용자 삭제 성공")
    @ApiResponse(responseCode = "403", description = "사용자 삭제 권한 없음")
    @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    @Parameter(
            name = "userId",
            in = ParameterIn.PATH,
            description = "사용자 ID",
            schema = @Schema(
                    implementation = String.class,
                    format = "uuid"
            ),
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true
    )
    public ResponseEntity<Void> physicalDelete(
            @Parameter(hidden = true) @PathVariable UUID userId
    );

}
