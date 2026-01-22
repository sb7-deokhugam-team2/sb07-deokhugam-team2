package com.deokhugam.deokhugam.popularbook.integration.slice.jpa;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.enums.SortDirection;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.popularbook.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.popularbook.dto.response.CursorResult;
import com.deokhugam.domain.popularbook.dto.response.PopularBookAggregationDto;
import com.deokhugam.domain.popularbook.dto.response.PopularBookDto;
import com.deokhugam.domain.popularbook.entity.PopularBook;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.lang.Thread.sleep;
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

    @Nested
    @DisplayName("인기 도서 조회")
    class FindTopPopularBooks {
        @Test
        @DisplayName("인기 도서 조회 - windowStart 이후 스냅샷 중 latestCalculatedDate(최신 스냅샷)만 조회된다")
        void findTopPopularBooks_picksLatestSnapshotWithinWindow() {
            // given
            Book book1 = Book.create("t1", "a1", "isbn-1", LocalDate.now(), "p1", null, "d1");
            Book book2 = Book.create("t2", "a2", "isbn-2", LocalDate.now(), "p2", null, "d2");
            em.persist(book1);
            em.persist(book2);

            Instant now = Instant.now();
            Instant oldSnap = now.minus(10, ChronoUnit.MINUTES);
            Instant latestSnap = now.minus(2, ChronoUnit.MINUTES);

            // old snapshot (window 안에 있지만 최신은 아님)
            em.persist(PopularBook.create(PeriodType.DAILY, oldSnap, 1L, 10.0, 4.0, 2L, book1));
            em.persist(PopularBook.create(PeriodType.DAILY, oldSnap, 2L, 9.0, 3.5, 1L, book2));

            // latest snapshot
            em.persist(PopularBook.create(PeriodType.DAILY, latestSnap, 1L, 99.0, 5.0, 10L, book2));
            em.persist(PopularBook.create(PeriodType.DAILY, latestSnap, 2L, 98.0, 4.8, 9L, book1));

            em.flush();
            em.clear();

            PopularBookSearchCondition condition = new PopularBookSearchCondition(
                    PeriodType.DAILY,
                    SortDirection.ASC,
                    null,
                    null,
                    50
            );

            // when: windowStart를 oldSnap보다 더 과거로 주면 oldSnap도 후보지만 latestSnap만 선택돼야 함
            CursorResult<PopularBookDto> page = popularBookRepository.findTopPopularBooks(
                    condition,
                    now.minus(30, ChronoUnit.MINUTES),
                    PageRequest.of(0, 50)
            );

            // then: latestSnap의 데이터(점수 99/98)만 나와야 함
            assertThat(page.content()).hasSize(2);
            assertThat(page.content()).extracting(PopularBookDto::score)
                    .containsExactly(99.0, 98.0);
        }

        @Test
        @DisplayName("인기 도서 조회 - 커서(rank) + after(createdAt)로 다음 페이지 경계가 결정된다(동점 타이브레이커)")
        void findTopPopularBooks_cursorPagination_usesRankAndCreatedAtTiebreaker() throws InterruptedException {
            // given
            Book book1 = Book.create("t1", "a1", "isbn-1", LocalDate.now(), "p1", null, "d1");
            Book book2 = Book.create("t2", "a2", "isbn-2", LocalDate.now(), "p2", null, "d2");
            Book book3 = Book.create("t3", "a3", "isbn-3", LocalDate.now(), "p3", null, "d3");
            em.persist(book1);
            em.persist(book2);
            em.persist(book3);

            Instant snap = Instant.now().minus(1, ChronoUnit.MINUTES);

            // rank=1 두 개(동점), createdAt은 BaseEntity에 의해 자동으로 생성되므로
            // persist 순서로 createdAt이 미세하게라도 달라지는 걸 활용
            PopularBook popularBook1 = PopularBook.create(PeriodType.DAILY, snap, 1L, 10.0, 4.0, 2L, book1);
            PopularBook popularBook2 = PopularBook.create(PeriodType.DAILY, snap, 1L, 9.0, 4.0, 2L, book2);
            PopularBook popularBook3 = PopularBook.create(PeriodType.DAILY, snap, 2L, 8.0, 4.0, 2L, book3);
            em.persist(popularBook1);
            sleep(100);
            em.persist(popularBook2);
            sleep(100);
            em.persist(popularBook3);

            em.flush();
            em.clear();

            // 첫 페이지(ASC): rank asc, createdAt asc 라고 했으니
            // rank=1 두 개 중 createdAt 작은 게 먼저 올 가능성이 큼
            PopularBookSearchCondition firstCondition = new PopularBookSearchCondition(
                    PeriodType.DAILY,
                    SortDirection.ASC,
                    null,
                    null,
                    2
            );
            Instant windowStart1 = Instant.now().minus(10, ChronoUnit.MINUTES);
            Pageable pageable = PageRequest.of(0, 2);
            CursorResult<PopularBookDto> pagePopularBookDto1 = popularBookRepository.findTopPopularBooks(
                    firstCondition,
                    windowStart1,
                    pageable
            );

            assertThat(pagePopularBookDto1.content()).hasSize(2);

            // page1의 마지막 요소를 cursor/after로 사용
            PopularBookDto lastPopularBookDto = pagePopularBookDto1.content().get(pagePopularBookDto1.content().size() - 1);

            PopularBookSearchCondition next = new PopularBookSearchCondition(
                    PeriodType.DAILY,
                    SortDirection.ASC,
                    String.valueOf(lastPopularBookDto.rank()), // cursor = rank
                    lastPopularBookDto.createdAt(),            // after = createdAt
                    2
            );

            // when
            CursorResult<PopularBookDto> pagePopularBookDto2 = popularBookRepository.findTopPopularBooks(
                    next,
                    Instant.now().minus(10, ChronoUnit.MINUTES),
                    PageRequest.of(0, 2)
            );

            // then: 다음 페이지에는 "경계 이후"만 나와야 함(중복 X)
            assertThat(pagePopularBookDto2.content()).extracting(PopularBookDto::id)
                    .doesNotContainAnyElementsOf(pagePopularBookDto1.content().stream().map(PopularBookDto::id).toList());
        }

        @Test
        @DisplayName("인기 도서 조회 - DESC 정렬에서 rank desc, createdAt desc로 정렬되고 커서도 그 방향으로 동작한다")
        void findTopPopularBooks_descOrderingAndCursorWorks() {
            // given
            Book book1 = Book.create("t1", "a1", "isbn-1", LocalDate.now(), "p1", null, "d1");
            Book book2 = Book.create("t2", "a2", "isbn-2", LocalDate.now(), "p2", null, "d2");
            em.persist(book1);
            em.persist(book2);

            Instant snap = Instant.now().minus(1, ChronoUnit.MINUTES);

            em.persist(PopularBook.create(PeriodType.DAILY, snap, 1L, 10.0, 4.0, 2L, book1));
            em.persist(PopularBook.create(PeriodType.DAILY, snap, 2L, 9.0, 4.0, 2L, book2));

            em.flush();
            em.clear();

            PopularBookSearchCondition condition = new PopularBookSearchCondition(
                    PeriodType.DAILY,
                    SortDirection.DESC,
                    null,
                    null,
                    10
            );

            // when
            CursorResult<PopularBookDto> pagePopularBookDto = popularBookRepository.findTopPopularBooks(
                    condition,
                    Instant.now().minus(10, ChronoUnit.MINUTES),
                    PageRequest.of(0, 10)
            );

            // then: rank desc니까 2가 먼저
            assertThat(pagePopularBookDto.content()).extracting(PopularBookDto::rank)
                    .containsExactly(2L, 1L);
        }
    }


}
