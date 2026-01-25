package com.deokhugam.deokhugam.popularreview.integration.service;

import ch.qos.logback.core.status.InfoStatus;
import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.comment.repository.CommentRepository;
import com.deokhugam.domain.likedreview.entity.LikedReview;
import com.deokhugam.domain.likedreview.repository.LikedReviewRepository;
import com.deokhugam.domain.popularreview.dto.request.PopularReviewSearchCondition;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewPageResponseDto;
import com.deokhugam.domain.popularreview.entity.PopularReview;
import com.deokhugam.domain.popularreview.repository.PopularReviewRepository;
import com.deokhugam.domain.popularreview.service.PopularReviewBatchService;
import com.deokhugam.domain.popularreview.service.PopularReviewService;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.enums.SortDirection;
import com.deokhugam.domain.review.repository.ReviewRepository;
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
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
@Transactional
public class PopularReviewIntegrationTest {
    @Autowired
    PopularReviewBatchService popularReviewBatchService;

    @Autowired
    PopularReviewService popularReviewService;

    @Autowired
    PopularReviewRepository popularReviewRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    LikedReviewRepository likedReviewRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("배치 실행 후 인기 리뷰 조회가 가능하다.")
    void batch_then_search_success() {
        // given
        User user = User.create("user@naver.com", "nickname", "password123!");
        userRepository.save(user);

        Book book = Book.create("title",
                "author",
                "123213-1321",
                LocalDate.now(),
                "publisher",
                "books/abc.jpg",
                "description");
        bookRepository.save(book);

        Review review = Review.create(4.5, "content", book, user);
        reviewRepository.save(review);

        PopularReviewSearchCondition condition = new PopularReviewSearchCondition(
                PeriodType.ALL_TIME,
                SortDirection.ASC,
                null,
                null,
                10
        );

        popularReviewBatchService.calculateAndSaveAllPeriods();

        // when
        PopularReviewPageResponseDto page = popularReviewService.getPopularReviews(condition);

        // then
        assertThat(page).isNotNull();
        assertThat(page.content()).isNotNull();

    }

    @Test
    @DisplayName("배치 실행 - period별로 score가 계산되고 저장된다.")
    void batch_calculate_and_saveAll_period_score_success() {
        // given
        User author = userRepository.save(User.create("author@naver.com", "nickname1", "password123!"));
        User oldUser = userRepository.save(User.create("olduser@naver.com", "nickname2", "password123!"));
        User newUser = userRepository.save(User.create("newuser@naver.com", "nickname3", "password123!"));

        Book book = bookRepository.save(Book.create("title",
                "author",
                "123213-1321",
                LocalDate.now(),
                "publisher",
                "books/abc.jpg",
                "description"));

        Review review = reviewRepository.save(Review.create(4.5, "content", book, author));

        Instant calculatedDate = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .atStartOfDay(ZoneId.of("Asia/Seoul"))
                .toInstant();

        Instant yesterday = calculatedDate.minusSeconds(60);
        Instant today = calculatedDate.plusSeconds(60);

        LikedReview oldLikedReview = likedReviewRepository.save(LikedReview.create(review, oldUser));
        LikedReview newLikedReview = likedReviewRepository.save(LikedReview.create(review, newUser));

        Comment oldComment = commentRepository.save(Comment.create("oldContent", oldUser, review));
        Comment newComment = commentRepository.save(Comment.create("newContent", newUser, review));

        updateLikedReviewUpdatedAt(oldLikedReview.getId(), yesterday);
        updateLikedReviewUpdatedAt(newLikedReview.getId(), today);

        updateCommentCreatedAt(oldComment.getId(), yesterday);
        updateCommentCreatedAt(newComment.getId(), today);

        em.flush();
        em.clear();

        // when
        popularReviewBatchService.calculateAndSaveAllPeriods();
        em.flush();
        em.clear();

        // then
        PopularReview daily = popularReviewRepository.findAll().stream()
                .filter(pr -> pr.getPeriodType() == PeriodType.DAILY && pr.getCalculatedDate().equals(calculatedDate))
                .findFirst()
                .orElseThrow();

        assertThat(daily.getLikedCount()).isEqualTo(1L);
        assertThat(daily.getCommentCount()).isEqualTo(1L);
        assertThat(daily.getScore()).isCloseTo(1.0, within(0.0001));

        PopularReview weekly = popularReviewRepository.findAll().stream()
                .filter(pr -> pr.getPeriodType() == PeriodType.WEEKLY && pr.getCalculatedDate().equals(calculatedDate))
                .findFirst()
                .orElseThrow();

        assertThat(weekly.getLikedCount()).isEqualTo(1L);
        assertThat(weekly.getCommentCount()).isEqualTo(1L);
        assertThat(weekly.getScore()).isCloseTo(1.0, within(0.0001));

        PopularReview monthly = popularReviewRepository.findAll().stream()
                .filter(pr -> pr.getPeriodType() == PeriodType.MONTHLY && pr.getCalculatedDate().equals(calculatedDate))
                .findFirst()
                .orElseThrow();

        assertThat(monthly.getLikedCount()).isEqualTo(1L);
        assertThat(monthly.getCommentCount()).isEqualTo(1L);
        assertThat(monthly.getScore()).isCloseTo(1.0, within(0.0001));

        PopularReview allTime = popularReviewRepository.findAll().stream()
                .filter(pr -> pr.getPeriodType() == PeriodType.ALL_TIME && pr.getCalculatedDate().equals(calculatedDate))
                .findFirst()
                .orElseThrow();

        assertThat(allTime.getLikedCount()).isEqualTo(1L);
        assertThat(allTime.getCommentCount()).isEqualTo(1L);
        assertThat(allTime.getScore()).isCloseTo(1.0, within(0.0001));

    }

    private void updateLikedReviewUpdatedAt(UUID likedReviewId, Instant updatedAt) {
        em.createNativeQuery("update liked_reviews set updated_at = ?1 where id = ?2")
                .setParameter(1, updatedAt)
                .setParameter(2, likedReviewId)
                .executeUpdate();
    }

    private void updateCommentCreatedAt(UUID commentId, Instant createdAt) {
        em.createNativeQuery("update comments set created_at = ?1 where id = ?2")
                .setParameter(1, createdAt)
                .setParameter(2, commentId)
                .executeUpdate();
    }
}
