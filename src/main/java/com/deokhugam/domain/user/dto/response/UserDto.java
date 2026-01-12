package com.deokhugam.domain.user.dto.response;

import com.deokhugam.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class UserDto {
    private UUID id;
    private String email;
    private String nickname;
    private Instant createdAt;

    public static UserDto from(User user){
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }
}
