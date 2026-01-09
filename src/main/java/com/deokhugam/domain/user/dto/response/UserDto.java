package com.deokhugam.domain.user.dto.response;

import com.deokhugam.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
public class UserDto {
    private UUID id;
    private String email;
    private String nickname;
    private Instant createdAt;

    public static UserDto from(User user){
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
