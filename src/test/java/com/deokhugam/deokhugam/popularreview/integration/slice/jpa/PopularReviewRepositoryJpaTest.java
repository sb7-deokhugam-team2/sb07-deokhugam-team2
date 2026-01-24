package com.deokhugam.deokhugam.popularreview.integration.slice.jpa;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.popularreview.dto.request.PopularReviewSearchCondition;
import com.deokhugam.domain.popularreview.dto.response.PopularReviewPageResponseDto;
import com.deokhugam.domain.popularreview.entity.PopularReview;
import com.deokhugam.domain.popularreview.repository.PopularReviewRepository;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.enums.SortDirection;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.config.JpaAuditingConfig;
import com.deokhugam.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
public class PopularReviewRepositoryJpaTest {
    @Autowired
    PopularReviewRepository popularReviewRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("인기 리뷰 조회 - period 최신 calculatedDate만 조회된다.")
    void searchPopularReviews_latestCalculatedDate_Only() {
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

        Instant oldDate = Instant.parse("2026-01-20T00:00:00Z");
        Instant newDate = Instant.parse("2026-01-24T00:00:00Z");

        PopularReview old1 = PopularReview.create(PeriodType.DAILY, oldDate, 1L, 10.0, 1L, 1L, review);
        PopularReview new1 = PopularReview.create(PeriodType.DAILY, newDate, 1L, 20.0, 2L, 3L, review);

        popularReviewRepository.save(old1);
        popularReviewRepository.save(new1);
        popularReviewRepository.flush();

        PopularReviewSearchCondition condition = new PopularReviewSearchCondition(
                PeriodType.DAILY,
                SortDirection.ASC,
                null,
                null,
                10
        );

        // when
        PopularReviewPageResponseDto page = popularReviewRepository.searchPopularReviews(condition);

        // then
        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0).createdAt()).isEqualTo(newDate);
        assertThat(page.content().get(0).rank()).isEqualTo(1L);

    }

    @Test
    @DisplayName("인기 리뷰 조회 - rank를 cursor로 다음 페이지가 이어진다.")
    void searchPopularReviews_cursorPagination() {
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

        Review review1 = Review.create(4.0, "content1", book, user);
        Review review2 = Review.create(4.0, "content2", book, user);
        Review review3 = Review.create(4.0, "content3", book, user);
        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

        Instant date =  Instant.parse("2026-01-24T00:00:00Z");

        PopularReview popularReview1 = PopularReview.create(PeriodType.DAILY, date, 1L, 10.0, 1L, 1L, review1);
        PopularReview popularReview2 = PopularReview.create(PeriodType.DAILY, date, 2L, 9.0, 1L, 1L, review2);
        PopularReview popularReview3 = PopularReview.create(PeriodType.DAILY, date, 3L, 8.0, 1L, 1L, review3);
        popularReviewRepository.save(popularReview1);
        popularReviewRepository.save(popularReview2);
        popularReviewRepository.save(popularReview3);
        popularReviewRepository.flush();

        PopularReviewSearchCondition first = new PopularReviewSearchCondition(
                PeriodType.DAILY,
                SortDirection.ASC,
                null,
                null,
                2
        );

        // when
        PopularReviewPageResponseDto firstPage = popularReviewRepository.searchPopularReviews(first);

        PopularReviewSearchCondition second = new PopularReviewSearchCondition(
                PeriodType.DAILY,
                SortDirection.ASC,
                firstPage.nextCursor(),
                null,
                2
        );

        PopularReviewPageResponseDto secondPage = popularReviewRepository.searchPopularReviews(second);

        // then
        assertThat(firstPage.content()).hasSize(2);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.nextCursor()).isEqualTo("2");

        assertThat(secondPage.content()).hasSize(1);
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.content().get(0).rank()).isEqualTo(3L);
    }
}
