package com.deokhugam.domain.poweruser.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserReviewScoreDto {
    private UUID userId;
    private Double score;
}
