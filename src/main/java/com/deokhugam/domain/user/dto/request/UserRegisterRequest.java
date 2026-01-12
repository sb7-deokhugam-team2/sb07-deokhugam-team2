package com.deokhugam.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
        @Email
        @NotBlank
        String email,

        @Size(min = 2, max = 20)
        @NotBlank
        String nickname,

        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
                message = "비밀번호는 8~20자이며, 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다."
        )
        String password
) {}
