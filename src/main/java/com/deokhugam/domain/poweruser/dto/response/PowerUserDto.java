package com.deokhugam.domain.poweruser.dto.response;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.poweruser.entity.PowerUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PowerUserDto {
    private UUID userId;
    private String nickname;
    private PeriodType periodType;
    private Instant createdAt;
    private Long rank;
    private Double score;
    private Long likeCount;
    private Long commentCount;
    private Long reviewScoreSum;

    public static PowerUserDto from(PowerUser powerUser) {
        return new PowerUserDto(
                powerUser.getUser().getId(),
                powerUser.getUser().getNickname(),
                powerUser.getPeriodType(),
                powerUser.getCreatedAt(),
                powerUser.getRank(),
                powerUser.getScore(),
                powerUser.getLikedCount(),
                powerUser.getCommentCount(),
                powerUser.getReviewScoreSum()
        );
    }
}
