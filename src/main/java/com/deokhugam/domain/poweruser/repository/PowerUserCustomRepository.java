package com.deokhugam.domain.poweruser.repository;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.poweruser.dto.request.PowerUserSearchCondition;
import com.deokhugam.domain.poweruser.entity.PowerUser;
import com.deokhugam.domain.poweruser.repository.dto.UserCommentCountDto;
import com.deokhugam.domain.poweruser.repository.dto.UserLikeCountDto;
import com.querydsl.core.types.Projections;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.deokhugam.domain.comment.entity.QComment.comment;
import static com.deokhugam.domain.likedreview.entity.QLikedReview.likedReview;

public interface PowerUserCustomRepository {
    Map<UUID, Long> getUserLikedCount(Instant time);

    Map<UUID, Long> getUserCommentCount(Instant time);

    Map<UUID, Double> getUserReviewScore(Instant time);

    List<PowerUser> searchPowerUsers(PowerUserSearchCondition condition);
}
