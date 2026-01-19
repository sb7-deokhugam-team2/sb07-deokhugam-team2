package com.deokhugam.domain.poweruser.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class UserCommentCountDto {
    private UUID userId;
    private Long likedCount;
}
