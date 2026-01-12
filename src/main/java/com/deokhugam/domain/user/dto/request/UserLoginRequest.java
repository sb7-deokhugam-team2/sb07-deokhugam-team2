package com.deokhugam.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}
