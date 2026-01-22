package com.deokhugam.deokhugam.likedreview.integration.slice.jpa;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.likedreview.entity.LikedReview;
import com.deokhugam.domain.likedreview.repository.LikedReviewRepository;
import com.deokhugam.domain.review.entity.Review;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
public class LikedReviewRepositoryTest {
    @Autowired
    LikedReviewRepository likedReviewRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("리뷰 좋아요의 리뷰와 유저의 아이디가 정상적으로 조회된다.")
    void findByReviewIdAndUserId_success() {
        // given
        User user = userRepository.save(User.create("user@naver.com", "nickname", "password123!"));
        Book book = bookRepository.save(Book.create(
                "title",
                "author",
                "234234-423432",
                LocalDate.now(),
                "publisher",
                "thumbnailUrl",
                "description"
                ));
        Review review = reviewRepository.save(Review.create(4.5, "comment", book, user));

        likedReviewRepository.saveAndFlush(LikedReview.create(review, user));

        // when
        Optional<LikedReview> found = likedReviewRepository.findByReviewIdAndUserId(review.getId(), user.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().isLiked()).isTrue();
    }

    @Test
    @DisplayName("같은 리뷰에 좋아요가 두번 저장되면 예외")
    void sameReviewAndUser_saveTwice_exception() {
        // given
        User user = userRepository.save(User.create("user@naver.com", "nickname", "password123!"));
        Book book = bookRepository.save(Book.create(
                "title",
                "author",
                "234234-423432",
                LocalDate.now(),
                "publisher",
                "thumbnailUrl",
                "description"
        ));
        Review review = reviewRepository.save(Review.create(4.5, "comment", book, user));

        likedReviewRepository.saveAndFlush(LikedReview.create(review, user));

        // when / then
        assertThatThrownBy(() ->
                likedReviewRepository.saveAndFlush(LikedReview.create(review, user))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("좋아요가 toggle되면 like 값이 바뀐다.")
    void toggle_changed_likedValue() {
        // given
        User user = userRepository.save(User.create("user@naver.com", "nickname", "password123!"));
        Book book = bookRepository.save(Book.create(
                "title",
                "author",
                "234234-423432",
                LocalDate.now(),
                "publisher",
                "thumbnailUrl",
                "description"
        ));
        Review review = reviewRepository.save(Review.create(4.5, "comment", book, user));

        LikedReview likedReview = likedReviewRepository.saveAndFlush(LikedReview.create(review, user));

        // when
        likedReview.toggle();
        likedReviewRepository.flush();

        // then
        LikedReview reloaded = likedReviewRepository.findById(likedReview.getId()).orElseThrow();
        assertThat(reloaded.isLiked()).isFalse();
    }

}
