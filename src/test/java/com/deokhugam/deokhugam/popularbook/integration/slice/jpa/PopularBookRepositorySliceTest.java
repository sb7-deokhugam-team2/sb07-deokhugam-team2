package com.deokhugam.deokhugam.popularbook.integration.slice.jpa;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.popularbook.dto.response.PopularBookAggregationDto;
import com.deokhugam.domain.popularbook.repository.PopularBookRepository;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.global.config.JpaAuditingConfig;
import com.deokhugam.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
public class PopularBookRepositorySliceTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private PopularBookRepository popularBookRepository;
    @Autowired
    private BookRepository bookRepository;

    @Nested
    @DisplayName("인기 도서 연산 - findTopPopularBookAggregate")
    class FindTopPopularBookAggregate {

        @Test
        @DisplayName("인기 도서 연산 조회 - DAILY 기간 윈도우 밖 리뷰는 집계에서 제외된다")
        void findTopPopularBookAggregate_filtersOutReviewOutsideWindow() throws Exception {
            // given
            Book book1 = Book.create("title", "author", "isbn-1", LocalDate.now(), "publisher", null, "description");
            em.persist(book1);

            User user1 = User.create("test@test.com", "test", "12345678a!");
            User user2 = User.create("test2@test.com", "test2", "12345678a!");
            User user3 = User.create("test3@test.com", "test3", "12345678a!");
            em.persist(user1);
            em.persist(user2);
            em.persist(user3);


            Review review1 = Review.create(5.0, "content", book1, user1);
            Review review2 = Review.create(1.0, "content", book1, user2);
            Review review3 = Review.create(5.0, "content", book1, user3);
            em.persist(review1);
            em.persist(review2);
            em.persist(review3);

            em.flush();

            em.createNativeQuery("""
                                update reviews
                                set created_at = ?1
                                where id = ?2
                            """)
                    .setParameter(1, Instant.now().minus(2, ChronoUnit.DAYS))
                    .setParameter(2, review3.getId())
                    .executeUpdate(); // private, @CreatedDate, updatable = false로 인해 값을 바꿀수없기에 native로 update문 요청하여 시간변경
            em.clear();

            // when
            List<PopularBookAggregationDto> result =
                    popularBookRepository.findTopPopularBookAggregates(PeriodType.DAILY, 10);

            // then
            PopularBookAggregationDto aggregationResult = result.stream()
                    .filter(dto -> dto.bookId().equals(book1.getId()))
                    .findFirst().orElseThrow();

            assertThat(aggregationResult.reviewCount()).isEqualTo(2L); // review3 제외
        }

        @Test
        @DisplayName("인기 도서 연산 조회 - 논리 삭제된 리뷰는 집계에서 제외된다")
        void findTopPopularBookAggregate_excludesSoftDeletedReview() {
            // given
            Book book1 = Book.create("title", "author", "isbn-1", LocalDate.now(), "publisher", null, "description");
            em.persist(book1);

            User user1 = User.create("test@test.com", "test", "12345678a!");
            User user2 = User.create("test2@test.com", "test2", "12345678a!");
            em.persist(user1);
            em.persist(user2);


            Review review1 = Review.create(5.0, "content", book1, user1);
            Review review2 = Review.create(1.0, "content", book1, user2);
            review2.delete(); // 논리 삭제
            em.persist(review1);
            em.persist(review2);

            em.flush();
            em.clear();

            // when
            List<PopularBookAggregationDto> result =
                    popularBookRepository.findTopPopularBookAggregates(PeriodType.DAILY, 10);

            // then
            PopularBookAggregationDto aggregationResult = result.stream()
                    .filter(dto -> dto.bookId().equals(book1.getId()))
                    .findFirst().orElseThrow();

            assertThat(aggregationResult.reviewCount()).isEqualTo(1L); // 논리삭제 리뷰 제외
        }

        @Test
        @DisplayName("인기 도서 연산 조회 - 논리 삭제된 도서는 집계 결과에서 제외된다")
        void findTopPopularBookAggregate_excludesSoftDeletedBook() {
            // given
            Book book1 = Book.create("title", "author", "isbn-1", LocalDate.now(), "publisher", null, "description");
            Book book3 = Book.create("title3", "author3", "isbn-3", LocalDate.now().minusDays(2), "publisher3", null, "description3");
            book3.delete(); // 삭제 도서

            em.persist(book1);
            em.persist(book3);

            User user1 = User.create("test@test.com", "test", "12345678a!");
            em.persist(user1);


            // book1에는 정상 리뷰 1개
            Review review1 = Review.create(5.0, "content", book1, user1);
            em.persist(review1);

            // 삭제된 book3에도 리뷰 1개 달아두기(그래도 book.isDeleted 조건으로 제외돼야 함)
            Review review3 = Review.create(5.0, "content", book3, user1);
            em.persist(review3);

            em.flush();
            em.clear();

            // when
            List<PopularBookAggregationDto> aggregationResult =
                    popularBookRepository.findTopPopularBookAggregates(PeriodType.DAILY, 10);

            // then
            assertThat(aggregationResult).extracting(PopularBookAggregationDto::bookId)
                    .contains(book1.getId())
                    .doesNotContain(book3.getId());
        }
    }


}
