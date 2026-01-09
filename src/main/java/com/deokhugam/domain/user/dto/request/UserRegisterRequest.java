package com.deokhugam.domain.user.dto.request;

public record UserRegisterRequest(
        String email,
        String nickname,
        String password
) {}
