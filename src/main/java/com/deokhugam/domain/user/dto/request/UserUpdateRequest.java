package com.deokhugam.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(min = 2, max = 20)
        @NotBlank
        @JsonValue
        String nickname
) {
    @JsonCreator
    public UserUpdateRequest(String nickname) {
        this.nickname = nickname;
    }
}
