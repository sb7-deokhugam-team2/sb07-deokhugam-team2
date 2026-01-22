package com.deokhugam.deokhugam.poweruser.integration.service;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.poweruser.dto.request.PowerUserSearchCondition;
import com.deokhugam.domain.poweruser.dto.response.CursorPageResponsePowerUserDto;
import com.deokhugam.domain.poweruser.entity.PowerUser;
import com.deokhugam.domain.poweruser.enums.PowerUserDirection;
import com.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.deokhugam.domain.poweruser.service.PowerUserService;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("PowerUserServiceIntegrationTest")
public class PowerUserServiceIntegrationTest {
    @Autowired
    PowerUserRepository powerUserRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EntityManager em;
    @Autowired
    PowerUserService powerUserService;

    @Test
    @DisplayName("커서 페이지네이션 조회 - 성공")
    void findPowerUsers() {
        //given
        User user = User.create("test1234@gmail.com", "nickname", "password1!");
        userRepository.save(user);
        PowerUser powerUser = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 1L, 100.0, 0L, 0L, 200.0, user);
        PowerUser powerUser2 = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 1L, 50.0, 0L, 0L, 100.0, user);
        PowerUser powerUser3 = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 1L, 40.0, 0L, 0L, 80.0, user);
        powerUserRepository.save(powerUser);
        powerUserRepository.save(powerUser2);
        powerUserRepository.save(powerUser3);
        em.flush();
        em.clear();

        PowerUserSearchCondition condition = new PowerUserSearchCondition(PeriodType.ALL_TIME, PowerUserDirection.DESC, null, null, 10);

        //when
        CursorPageResponsePowerUserDto result = powerUserService.findPowerUsers(condition);

        //then
        assertThat(result.getContent()).extracting("nickname").containsExactly("nickname", "nickname", "nickname");
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getNextAfter()).isEqualTo(powerUser3.getCreatedAt());
        assertThat(result.getNextCursor()).isEqualTo(powerUser3.getCreatedAt().toString());
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("기간 별 파워 유저 랭킹 계산 - 성공")
    void calculateRankingByPeriod() {
        //given
        User user = User.create("test@gmail.com", "test", "12345678a!");
        User user2 = User.create("test2@gmail.com", "test2", "12345678a!");
        User user3 = User.create("test3@gmail.com", "test", "12345678a!");
        User user4 = User.create("test4@gmail.com", "test", "12345678a!");
        Book book = Book.create("title",
                "content",
                "12345678",
                LocalDate.now().minusDays(2),
                "publisher",
                "thumbnailUrl",
                "description");
        Review review = Review.create(5.0, "content", book, user2);
        em.persist(user);
        em.persist(user2);
        em.persist(user3);
        em.persist(user4);
        em.persist(book);
        em.persist(review);

        for (int i = 0; i < 10; i++) {
            Comment comment = Comment.create("content" + i, user, review);
            em.persist(comment);
        }
        em.flush();
        em.clear();

        //when
        powerUserService.calculateRankingByPeriod(PeriodType.ALL_TIME, ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS));

        //then
        PowerUserSearchCondition condition = new PowerUserSearchCondition(PeriodType.ALL_TIME, PowerUserDirection.ASC, null, null, 10);
        CursorPageResponsePowerUserDto result = powerUserService.findPowerUsers(condition);
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(1).getScore()).isEqualTo(10 * 0.3);
        assertThat(result.getContent()).extracting("nickname").containsExactly("test2", "test");
    }
}
