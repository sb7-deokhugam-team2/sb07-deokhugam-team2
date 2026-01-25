package com.deokhugam.deokhugam.review.integration.slice.jpa;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.repository.ReviewRepository;
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

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
public class ReviewRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("성공: 삭제되지 않는  리뷰만 조회 가능")
    void findByIdAndIsDeletedFalse_Success() {
        // given
        User user = User.create("test@gmail.com", "nickname", "password123!");
        Book book = Book.create(
                "title",
                "author",
                "123456789",
                LocalDate.now(),
                "publisher",
                "thumbnailUrl",
                "description"
        );
        Review review = Review.create(4.0, "리뷰 내용", book, user);

        userRepository.save(user);
        bookRepository.save(book);
        Review savedReview = reviewRepository.save(review);

        em.flush();
        em.clear();

        // when
        Optional<Review> result = reviewRepository.findByIdAndIsDeletedFalse(savedReview.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedReview.getId());
        assertThat(result.get().isDeleted()).isFalse();
    }

    @Test
    @DisplayName("실패: 논리 삭제된 리뷰는 조회되지 않음")
    void findByIdAndIsDeletedFalse_Fail_DeletedReview() {
        // given
        User user = User.create("test@gmail.com", "nickname", "password123!");
        Book book = Book.create(
                "title",
                "author",
                "123456789",
                LocalDate.now(),
                "publisher",
                "thumbnailUrl",
                "description"
        );

        userRepository.save(user);
        bookRepository.save(book);

        Review review = Review.create(4.0, "리뷰 내용", book, user);
        Review savedReview = reviewRepository.save(review);

        savedReview.delete();
        reviewRepository.save(savedReview);

        em.flush();
        em.clear();

        // when
        Optional<Review> result = reviewRepository.findByIdAndIsDeletedFalse(savedReview.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공: 특정 사용자와 책으로 리뷰 존재 확인")
    void existsReviewByUserIdAndBookId_Success() {
        // given
        User user = User.create("test@gmail.com", "nickname", "password123!");
        Book book = Book.create(
                "title",
                "author",
                "1234567890",
                LocalDate.now(),
                "publisher",
                "thumbnailUrl",
                "description"
        );

        userRepository.save(user);
        bookRepository.save(book);

        Review review = Review.create(4.0, "리뷰 내용", book, user);
        reviewRepository.save(review);

        em.flush();
        em.clear();

        // when
        boolean exists = reviewRepository.existsReviewByUserIdAndBookIdAndIsDeletedFalse(user.getId(), book.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("실패: 존재하지 않는 사용자와 책 조합으로 리뷰 조회")
    void existsReviewByUserIdAndBookId_Fail() {
        // given
        UUID nonExistentUserId = UUID. randomUUID();
        UUID nonExistentBookId = UUID. randomUUID();

        // when
        boolean exists = reviewRepository. existsReviewByUserIdAndBookIdAndIsDeletedFalse(nonExistentUserId, nonExistentBookId);

        // then
        assertThat(exists).isFalse();
    }
}

