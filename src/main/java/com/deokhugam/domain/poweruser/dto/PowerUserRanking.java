package com.deokhugam.domain.poweruser.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PowerUserRanking {
    private UUID userId;
    private Long likedCount;
    private Long commentCount;
    private Double reviewScoreSum;
    private Double totalScore;
}
