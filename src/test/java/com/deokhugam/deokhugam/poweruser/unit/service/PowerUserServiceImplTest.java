package com.deokhugam.deokhugam.poweruser.unit.service;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.poweruser.dto.request.PowerUserSearchCondition;
import com.deokhugam.domain.poweruser.dto.response.CursorPageResponsePowerUserDto;
import com.deokhugam.domain.poweruser.entity.PowerUser;
import com.deokhugam.domain.poweruser.enums.PowerUserDirection;
import com.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.deokhugam.domain.poweruser.service.PowerUserServiceImpl;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("파워 유저 서비스 클래스 유닛 테스트")
public class PowerUserServiceImplTest {
    @Mock
    PowerUserRepository powerUserRepository;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    PowerUserServiceImpl powerUserService;

    @Test
    @DisplayName("커서 페이지네이션 조회 - 성공")
    void findPowerUsers(){
        //given
        User user = User.create("test@gmail.com", "nickname", "password1!");
        List<PowerUser> powerUsers = new ArrayList<>();
        PowerUser powerUser = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 1L, 100.0, 0L, 0L, 200.0, user);
        PowerUser powerUser2 = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 1L, 50.0, 0L, 0L, 100.0, user);
        PowerUser powerUser3 = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 1L, 40.0, 0L, 0L, 80.0, user);

        ReflectionTestUtils.setField(powerUser, "createdAt", Instant.now());
        ReflectionTestUtils.setField(powerUser2, "createdAt", Instant.now());
        ReflectionTestUtils.setField(powerUser3, "createdAt", Instant.now());

        powerUsers.add(powerUser);
        powerUsers.add(powerUser2);
        powerUsers.add(powerUser3);

        PowerUserSearchCondition condition = new PowerUserSearchCondition(PeriodType.ALL_TIME, PowerUserDirection.DESC, null, null, 10);
        when(powerUserRepository.searchPowerUsers(condition))
                .thenReturn(powerUsers);
        when(powerUserRepository.countByPeriodTypeAndCalculatedDate(PeriodType.ALL_TIME))
                .thenReturn(3L);
        //when
        CursorPageResponsePowerUserDto result = powerUserService.findPowerUsers(condition);

        //then
        assertThat(result.getContent()).extracting("nickname").containsExactly("nickname", "nickname", "nickname");
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getNextAfter()).isEqualTo(powerUser3.getCreatedAt());
        assertThat(result.getNextCursor()).isEqualTo(powerUser3.getCreatedAt().toString());
        assertThat(result.getTotalElements()).isEqualTo(3);
        verify(powerUserRepository).searchPowerUsers(condition);
        verify(powerUserRepository).countByPeriodTypeAndCalculatedDate(any(PeriodType.class));
     }

     @Test
     @DisplayName("기간 별 파워 유저 랭킹 계산 - 성공")
     void calculateRankingByPeriod(){
         //given
         UUID userId = UUID.randomUUID();
         UUID userId2 = UUID.randomUUID();
         UUID userId3 = UUID.randomUUID();
         Map<UUID, Long> userLikedCount= new HashMap<>();
         userLikedCount.put(userId, 10L);
         Map<UUID, Long> userCommentCount= new HashMap<>();
         userCommentCount.put(userId2, 10L);
         Map<UUID, Double> userReviewScore= new HashMap<>();
         userReviewScore.put(userId3, 10.0);

         when(powerUserRepository.getUserLikedCount(any(Instant.class)))
                 .thenReturn(userLikedCount);
         when(powerUserRepository.getUserCommentCount(any(Instant.class)))
                 .thenReturn(userCommentCount);
         when(powerUserRepository.getUserReviewScore(any(Instant.class)))
                 .thenReturn(userReviewScore);
         when(userRepository.getReferenceById(any(UUID.class)))
                 .thenReturn(null);


         //when
         powerUserService.calculateRankingByPeriod(PeriodType.DAILY, ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS));

         //then
        verify(powerUserRepository).getUserLikedCount(any(Instant.class));
        verify(powerUserRepository).getUserCommentCount(any(Instant.class));
        verify(powerUserRepository).getUserReviewScore(any(Instant.class));
        verify(powerUserRepository).saveAll(any());
      }
}
