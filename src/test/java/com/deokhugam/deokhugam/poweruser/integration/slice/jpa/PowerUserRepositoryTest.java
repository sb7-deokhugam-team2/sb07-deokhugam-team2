package com.deokhugam.deokhugam.poweruser.integration.slice.jpa;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.likedreview.entity.LikedReview;
import com.deokhugam.domain.poweruser.dto.request.PowerUserSearchCondition;
import com.deokhugam.domain.poweruser.entity.PowerUser;
import com.deokhugam.domain.poweruser.enums.PowerUserDirection;
import com.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.config.JpaAuditingConfig;
import com.deokhugam.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
public class PowerUserRepositoryTest {

    @Autowired
    PowerUserRepository powerUserRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("파워 유저 목록 검색 - 성공")
    void searchPowerUsers(){
        User user = User.create("test@gmail.com", "test", "12121212qw!");
        User user2 = User.create("test2@gmail.com", "test2", "12121212qw!");
        User user3 = User.create("test3@gmail.com", "test3", "12121212qw!");
        PowerUser powerUser = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 1L, 100.0, 0L, 0L, 200.0, user);
        PowerUser powerUser2 = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 2L, 80.0, 0L, 0L, 160.0, user2);
        PowerUser powerUser3 = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 3L, 60.0, 0L, 0L, 120.0, user3);
        em.persist(user);
        em.persist(user2);
        em.persist(user3);
        em.persist(powerUser);
        em.persist(powerUser2);
        em.persist(powerUser3);

        em.flush();
        em.clear();

        PowerUserSearchCondition condition = new PowerUserSearchCondition(PeriodType.ALL_TIME, PowerUserDirection.DESC, null, null, 10);

        //when
        List<PowerUser> powerUsers = powerUserRepository.searchPowerUsers(condition);

        //then
        boolean userIsLoaded = em.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(powerUsers.get(0).getUser());
        assertThat(powerUsers).extracting("rank").containsExactly(3L, 2L, 1L);
        assertThat(userIsLoaded).isTrue();
    }

    @Test
    @DisplayName("기간 별 유저의 좋아요 수 조회 -성공")
    void getUserLikedCount(){
        User user = User.create("test@gmail.com", "test", "12121212qw!");
        User user2 = User.create("test2@gmail.com", "test2", "12121212qw!");
        User user3 = User.create("test3@gmail.com", "test3", "12121212qw!");
        User savedUser = userRepository.save(user);
        User savedUser2 = userRepository.save(user2);
        User savedUser3 = userRepository.save(user3);

        Book book = Book.create(
                "title", "content", "12345678",
                LocalDate.now(), "publisher",
                "thumbnailUrl", "description");
        em.persist(book);

        Review review = Review.create(5.0, "content", book, savedUser);
        em.persist(review);

        LikedReview likedReview = LikedReview.create(review, savedUser);
        LikedReview likedReview2 = LikedReview.create(review, savedUser2);
        em.persist(likedReview);
        em.persist(likedReview2);

        em.flush();
        em.clear();

        Map<UUID, Long> userLikedCount = powerUserRepository.getUserLikedCount(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant());

        assertThat(userLikedCount.get(savedUser.getId())).isEqualTo(1L);
        assertThat(userLikedCount.keySet())
                .hasSize(2)
                .containsExactlyInAnyOrder(savedUser.getId(), savedUser2.getId());
        assertThat(userLikedCount.get(savedUser3.getId())).isNull();
    }

    @Test
    @DisplayName("기간 별 유저의 댓글 수 조회 -성공")
    void getUserCommentCount(){
        User user = User.create("test@gmail.com", "test", "12121212qw!");
        User user2 = User.create("test2@gmail.com", "test2", "12121212qw!");
        User user3 = User.create("test3@gmail.com", "test3", "12121212qw!");
        User savedUser = userRepository.save(user);
        User savedUser2 = userRepository.save(user2);
        User savedUser3 = userRepository.save(user3);

        Book book = Book.create(
                "title", "content", "12345678",
                LocalDate.now(), "publisher",
                "thumbnailUrl", "description");
        em.persist(book);

        Review review = Review.create(5.0, "content", book, savedUser);
        em.persist(review);

        Comment comment = Comment.create("content", savedUser, review);
        Comment comment2 = Comment.create("content", savedUser2, review);
        Comment comment3 = Comment.create("content", savedUser3, review);
        Comment comment4 = Comment.create("content", savedUser3, review);
        Comment comment5 = Comment.create("content", savedUser3, review);
        em.persist(comment);
        em.persist(comment2);
        em.persist(comment3);
        em.persist(comment4);
        em.persist(comment5);



        em.flush();
        em.clear();

        Map<UUID, Long> userCommentCount = powerUserRepository.getUserCommentCount(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant());

        assertThat(userCommentCount.get(savedUser.getId())).isEqualTo(1L);
        assertThat(userCommentCount.get(savedUser2.getId())).isEqualTo(1L);
        assertThat(userCommentCount.get(savedUser3.getId())).isEqualTo(3L);
        assertThat(userCommentCount.keySet())
                .hasSize(3)
                .containsExactlyInAnyOrder(savedUser.getId(), savedUser2.getId(), savedUser3.getId());
    }

    @Test
    @DisplayName("기간 별 유저의 리뷰 점수 조회 -성공")
    void getUserReviewScore(){
        User user = User.create("test@gmail.com", "test", "12121212qw!");
        User user2 = User.create("test2@gmail.com", "test2", "12121212qw!");
        User user3 = User.create("test3@gmail.com", "test3", "12121212qw!");
        User savedUser = userRepository.save(user);
        User savedUser2 = userRepository.save(user2);
        User savedUser3 = userRepository.save(user3);

        Book book = Book.create(
                "title", "content", "12345678",
                LocalDate.now(), "publisher",
                "thumbnailUrl", "description");
        em.persist(book);

        Review review = Review.create(5.0, "content", book, savedUser);
        em.persist(review);

        Review review2 = Review.create(5.0, "content", book, savedUser2);
        em.persist(review2);

        LikedReview likedReview = LikedReview.create(review, savedUser);
        LikedReview likedReview2 = LikedReview.create(review, savedUser2);
        em.persist(likedReview);
        em.persist(likedReview2);


        Comment comment = Comment.create("content", savedUser, review);
        Comment comment2 = Comment.create("content", savedUser2, review);
        Comment comment3 = Comment.create("content", savedUser3, review);
        Comment comment4 = Comment.create("content", savedUser3, review);
        Comment comment5 = Comment.create("content", savedUser3, review);
        em.persist(comment);
        em.persist(comment2);
        em.persist(comment3);
        em.persist(comment4);
        em.persist(comment5);



        em.flush();
        em.clear();

        Map<UUID, Double> userReviewScore = powerUserRepository.getUserReviewScore(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant());

        assertThat(userReviewScore.get(savedUser.getId())).isEqualTo(5*0.7+2*0.3);
        assertThat(userReviewScore.keySet())
                .hasSize(1)
                .containsExactlyInAnyOrder(savedUser.getId());
    }

    @Test
    @DisplayName("기간 별 최신 파워 유저 개수 조회 - 성공")
    void countByPeriodTypeAndCalculateDate(){
        User user = User.create("test@gmail.com", "test", "12121212qw!");
        User user2 = User.create("test2@gmail.com", "test2", "12121212qw!");
        User user3 = User.create("test3@gmail.com", "test3", "12121212qw!");
        PowerUser powerUser = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 1L, 100.0, 0L, 0L, 200.0, user);
        PowerUser powerUser2 = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 2L, 80.0, 0L, 0L, 160.0, user2);
        PowerUser powerUser3 = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 3L, 60.0, 0L, 0L, 120.0, user3);
        em.persist(user);
        em.persist(user2);
        em.persist(user3);
        em.persist(powerUser);
        em.persist(powerUser2);
        em.persist(powerUser3);

        em.flush();
        em.clear();

        Long count = powerUserRepository.countByPeriodTypeAndCalculatedDate(PeriodType.ALL_TIME);

        assertThat(count).isEqualTo(3);
    }
}
