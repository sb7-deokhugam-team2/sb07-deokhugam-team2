package com.deokhugam.domain.poweruser.service;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.dto.response.CursorPageResponseCommentDto;
import com.deokhugam.domain.poweruser.dto.request.PowerUserSearchCondition;
import com.deokhugam.domain.poweruser.dto.response.CursorPageResponsePowerUserDto;
import com.deokhugam.domain.poweruser.dto.response.PowerUserDto;
import com.deokhugam.domain.poweruser.entity.PowerUser;
import com.deokhugam.domain.poweruser.dto.PowerUserRanking;
import com.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.deokhugam.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class PowerUserServiceImpl implements PowerUserService{

    private final PowerUserRepository powerUserRepository;
    private final UserRepository userRepository;

    @Override
    public CursorPageResponsePowerUserDto findPowerUsers(PowerUserSearchCondition condition) {
        List<PowerUserDto> contents = powerUserRepository.searchPowerUsers(condition)
                .stream()
                .map(PowerUserDto::from)
                .collect(Collectors.toList());

        boolean hasNext = contents.size() > condition.limit();

        if (contents.size() > condition.limit()) {
            contents.remove(contents.size() - 1);
        }

        String nextCursor = null;
        Instant nextAfter = null;

        if(!contents.isEmpty()){
            PowerUserDto lastItem = contents.get(contents.size() - 1);
            nextCursor = lastItem.getCreatedAt().toString();
            nextAfter = lastItem.getCreatedAt();
        }

        long totalElements = powerUserRepository.count();

        return CursorPageResponsePowerUserDto.from(
                contents,
                nextCursor,
                nextAfter,
                contents.size(),
                totalElements,
                hasNext
        );
    }

    @Override
    public void calculateRankingByPeriod(PeriodType period, ZonedDateTime time) {
        Instant validTime = null;
        switch (period) {
            case DAILY -> validTime = time.minusDays(1).toInstant();
            case WEEKLY -> validTime = time.minusWeeks(1).toInstant();
            case MONTHLY -> validTime = time.minusMonths(1).toInstant();
            case ALL_TIME -> {
            }
            default -> throw new IllegalArgumentException("지원하지 않는 기간 타입입니다.");
        }

        Map<UUID, Long> userLikedCount = powerUserRepository.getUserLikedCount(validTime);
        Map<UUID, Long> userCommentCount = powerUserRepository.getUserCommentCount(validTime);
        Map<UUID, Double> userReviewScore = powerUserRepository.getUserReviewScore(validTime);

        Set<UUID> allUserIds = new HashSet<>();
        allUserIds.addAll(userLikedCount.keySet());
        allUserIds.addAll(userCommentCount.keySet());
        allUserIds.addAll(userReviewScore.keySet());

        List<PowerUserRanking> ranker = allUserIds.stream()
                .map(userId -> {
                    Long likedCount = userLikedCount.getOrDefault(userId, 0L);
                    Long commentCount = userCommentCount.getOrDefault(userId, 0L);
                    Double reviewScoreSum = userReviewScore.getOrDefault(userId, 0.0);

                    Double totalScore = (likedCount * 0.2) + (commentCount * 0.3) + (reviewScoreSum * 0.5);

                    return new PowerUserRanking(userId, likedCount, commentCount, reviewScoreSum, totalScore);
                })
                .sorted(Comparator.comparing(PowerUserRanking::getTotalScore).reversed())
                .limit(10)
                .toList();

        List<PowerUser> powerUsers = new ArrayList<>();
        for (int i = 0; i < ranker.size(); i++) {
            PowerUserRanking powerUserRanking = ranker.get(i);
            PowerUser powerUser = PowerUser.create(period,
                    time.toInstant(),
                    (long) i + 1,
                    powerUserRanking.getTotalScore(),
                    powerUserRanking.getLikedCount(),
                    powerUserRanking.getCommentCount(),
                    powerUserRanking.getReviewScoreSum().longValue(),
                    userRepository.getReferenceById(powerUserRanking.getUserId())
            );
            powerUsers.add(powerUser);
        }
        powerUserRepository.saveAll(powerUsers);
    }
}
