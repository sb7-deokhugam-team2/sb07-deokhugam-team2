package com.deokhugam.domain.poweruser.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReviewScoreDto {
    private UUID reviewId;
    private Double score;
}
