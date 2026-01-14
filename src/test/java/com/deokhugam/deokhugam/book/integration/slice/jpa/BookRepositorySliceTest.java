package com.deokhugam.deokhugam.book.integration.slice.jpa;

import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.enums.SortCriteria;
import com.deokhugam.domain.book.enums.SortDirection;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.config.JpaAuditingConfig;
import com.deokhugam.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
@DisplayName("BookRepository JPA Slice Test")
public class BookRepositorySliceTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    EntityManager em;
    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("도서등록 - BookSave")
    class saveBook {

        @Test
        @DisplayName("[Success] 도서 등록 - 도서등록후 같은 엔티티로 조회")
        void saveBook_should_success_and_same() {
            //given
            Book book = Book.create("title", "author", "1234567890123", LocalDate.now(), "publisher", null, "description");

            // when
            Book savedBook = bookRepository.save(book);
            em.flush();
            em.clear();

            Book found = bookRepository.findById(savedBook.getId()).orElseThrow();

            // then
            assertEquals(book.getId(), found.getId());
            assertEquals(book.getTitle(), found.getTitle());
            assertEquals(book.getAuthor(), found.getAuthor());
            assertEquals(book.getIsbn(), found.getIsbn());
            assertEquals(book.getPublishedDate(), found.getPublishedDate());
            assertEquals(book.getPublisher(), found.getPublisher());
            assertEquals(book.getDescription(), found.getDescription());
            assertNotNull(found.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("커서기반 도서목록 검색조회 - findBooks")
    class findBooks {

        @Test
        @DisplayName("[Success] 도서 목록 조회 - 목록20개에 대해 조건 없이 조회시 limit=10 만큼 반환하고, 다음 페이지 존재 여부(hasNext)를 계산한다")
        void findBooks_withoutCondition_whenGreaterThanLimit_returnsLimit_andHasNextTrue() {

            // given
            int limit = 10;
            BookSearchCondition condition = new BookSearchCondition(null, null, null, null, null, limit);
            Pageable pageable = PageRequest.of(0, limit);

            for (int i = 0; i < 20; i++) {
                bookRepository.save(Book.create(
                        "title-" + i,
                        "author",
                        "" + i,
                        LocalDate.of(2026, 1, 1),
                        "publisher",
                        null,
                        "description"
                ));

            }

            em.flush();
            em.clear();

            // when
            Page<BookDto> pageBooks = bookRepository.findBooks(condition, pageable);

            // then
            assertFalse(pageBooks.getContent().isEmpty());
            assertEquals(limit, pageBooks.getNumberOfElements());
            assertTrue(pageBooks.hasNext());

        }

        @Test
        @DisplayName("[Success] 도서 목록 조회 - 조건 없이 조회하면 저장된 개수(8개)만 반환하고 hasNext=false다")
        void findBooks_withoutCondition_whenLessThanLimit_returnsAll_andHasNextFalse() {

            // given
            int limit = 10;
            int savedCount = 8;
            BookSearchCondition condition = new BookSearchCondition(null, null, null, null, null, limit);
            Pageable pageable = PageRequest.of(0, limit);

            for (int i = 0; i < savedCount; i++) {
                bookRepository.save(Book.create(
                        "title-" + i,
                        "author",
                        String.valueOf(i),
                        LocalDate.of(2026, 1, 1),
                        "publisher",
                        null,
                        "description"
                ));
            }

            em.flush();
            em.clear();

            // when
            Page<BookDto> pageBooks = bookRepository.findBooks(condition, pageable);

            // then
            assertFalse(pageBooks.getContent().isEmpty());
            assertEquals(savedCount, pageBooks.getNumberOfElements());
            assertFalse(pageBooks.hasNext());

            // (선택) 실제 저장 개수와 일치 sanity check
            assertEquals(savedCount, pageBooks.getContent().size());
        }

        @Test
        @DisplayName("[Success] 도서 목록 조회 - keyword가 title에 포함되면 해당 도서만 조회된다")
        void findBooks_keyword_matchesTitle() {
            // given
            int limit = 10;
            String keyword = "Java";

            bookRepository.save(Book.create("Java in Action", "kim", "ISBN-1",
                    LocalDate.of(2026, 1, 1), "pub", null, "desc"));
            bookRepository.save(Book.create("Java Concurrency", "park", "ISBN-2",
                    LocalDate.of(2026, 1, 1), "pub", null, "desc"));
            bookRepository.save(Book.create("Spring Boot", "kim", "ISBN-3",
                    LocalDate.of(2026, 1, 1), "pub", null, "desc"));

            em.flush();
            em.clear();

            BookSearchCondition condition = new BookSearchCondition(keyword, null, null, null, null, limit);

            Pageable pageable = PageRequest.of(0, limit);

            // when
            Page<BookDto> result = bookRepository.findBooks(condition, pageable);

            // then
            assertEquals(2, result.getNumberOfElements());
            assertFalse(result.hasNext());

            assertTrue(result.getContent().stream().allMatch(dto -> dto.title().contains(keyword)));
        }

        @Test
        @DisplayName("[Success] 도서 목록 조회 - keyword가 author에 매칭되면 해당 도서가 조회된다")
        void findBooks_keyword_matchesAuthor() {
            // given
            int limit = 10;
            String keyword = "kim";

            bookRepository.save(Book.create("Title-1", "kim", "ISBN-1",
                    LocalDate.of(2026, 1, 1), "pub", null, "desc"));
            bookRepository.save(Book.create("Title-2", "kim", "ISBN-2",
                    LocalDate.of(2026, 1, 1), "pub", null, "desc"));
            bookRepository.save(Book.create("Title-3", "park", "ISBN-3",
                    LocalDate.of(2026, 1, 1), "pub", null, "desc"));

            em.flush();
            em.clear();

            BookSearchCondition condition = new BookSearchCondition(keyword, null, null, null, null, limit);
            Pageable pageable = PageRequest.of(0, limit);

            // when
            Page<BookDto> result = bookRepository.findBooks(condition, pageable);

            // then
            assertEquals(2, result.getNumberOfElements());
            assertFalse(result.hasNext());

            assertTrue(result.getContent().stream().allMatch(dto -> dto.author().contains(keyword)));
        }

        @Test
        @DisplayName("[Success] 도서 목록 조회 - keyword 검색 결과가 limit보다 많으면 limit만 반환하고 hasNext=true다")
        void findBooks_keyword_appliesLimit_andHasNext() {
            // given
            int limit = 10;
            int matchedCount = 15;
            String keyword = "Java";

            for (int i = 0; i < matchedCount; i++) {
                bookRepository.save(Book.create("Java-" + i, "author", "ISBN-" + i,
                        LocalDate.of(2026, 1, 1), "pub", null, "desc"));
            }
            // 미매칭 데이터도 섞어두기
            for (int i = 0; i < 5; i++) {
                bookRepository.save(Book.create("Spring-" + i, "author", "NOPE-" + i,
                        LocalDate.of(2026, 1, 1), "pub", null, "desc"));
            }

            em.flush();
            em.clear();

            BookSearchCondition condition = new BookSearchCondition(keyword, null, null, null, null, limit);
            Pageable pageable = PageRequest.of(0, limit);

            // when
            Page<BookDto> result = bookRepository.findBooks(condition, pageable);

            // then
            assertEquals(limit, result.getNumberOfElements());
            assertTrue(result.hasNext());
            assertTrue(result.getContent().stream().allMatch(dto -> dto.title().contains(keyword)));
        }

        @Test
        @DisplayName("[Success] 도서 목록 조회 - keyword 검색 시 title DESC 정렬이 적용된다")
        void findBooks_keyword_ordersByTitleDesc() {

            // given
            int limit = 10;
            String keyword = "Java";

            bookRepository.save(Book.create(
                    "Java Z", "author", "ISBN-1",
                    LocalDate.of(2026, 1, 1), "pub", null, "desc"
            ));
            bookRepository.save(Book.create(
                    "Java M", "author", "ISBN-2",
                    LocalDate.of(2026, 1, 1), "pub", null, "desc"
            ));
            bookRepository.save(Book.create(
                    "Java A", "author", "ISBN-3",
                    LocalDate.of(2026, 1, 1), "pub", null, "desc"
            ));

            em.flush();
            em.clear();

            BookSearchCondition condition =
                    new BookSearchCondition(keyword, SortCriteria.TITLE, SortDirection.DESC, null, null, limit);

            Pageable pageable = PageRequest.of(0, limit);

            // when
            Page<BookDto> result = bookRepository.findBooks(condition, pageable);

            // then
            List<String> titles = result.getContent()
                    .stream()
                    .map(BookDto::title)
                    .toList();

            assertThat(titles).containsExactly("Java Z", "Java M", "Java A");
        }

        @Test
        @DisplayName("[Success] 도서 목록 조회 - title이 같으면 createdAt DESC로 정렬된다 (타이브레이커)")
        void findBooks_ordersByCreatedAt_whenTitleSame() throws InterruptedException {

            // given
            int limit = 10;
            String keyword = "Java";

            bookRepository.save(Book.create(
                    "Java", "author", "ISBN-1",
                    LocalDate.of(2026, 1, 1), "pub", null, "desc"
            ));

            sleep(10); // createdAt 차이 만들기

            bookRepository.save(Book.create(
                    "Java", "author", "ISBN-2",
                    LocalDate.of(2026, 1, 1), "pub", null, "desc"
            ));

            em.flush();
            em.clear();

            BookSearchCondition condition =
                    new BookSearchCondition(keyword, SortCriteria.TITLE, null, null, null, limit);

            Pageable pageable = PageRequest.of(0, limit);

            // when
            Page<BookDto> result = bookRepository.findBooks(condition, pageable);

            // then
            List<Instant> createdAtList = result.getContent()
                    .stream()
                    .map(BookDto::createdAt)
                    .toList();

            assertTrue(createdAtList.get(0).isAfter(createdAtList.get(1))); // 더 나중에 저장된 것이 먼저
        }

        @Test
        @DisplayName("[Success] 도서 목록 조회 - keyword 검색 시 rating DESC 정렬이 적용된다")
        void findBooks_ordersByRatingDesc() {

            // given
            int limit = 10;
            String keyword = "Java";

            Book book1 = bookRepository.save(Book.create(
                    "Java A", "author", "ISBN-1",
                    LocalDate.of(2026, 1, 1), "pub", null, "desc"
            ));
            Book book2 = bookRepository.save(Book.create(
                    "Java B", "author", "ISBN-2",
                    LocalDate.of(2026, 1, 1), "pub", null, "desc"
            ));

            User user1 = userRepository.save(
                    User.create("emaile@email.com", "user1", "12345678q!")
            );

            User user2 = userRepository.save(
                    User.create("test@email.com", "user2", "123123dd@!")
            );

            reviewRepository.save(Review.create(5.0, "review_content!", book1, user1));
            reviewRepository.save(Review.create(3.0, "review_content!", book2, user2));

            em.flush();
            em.clear();

            BookSearchCondition condition =
                    new BookSearchCondition(keyword, SortCriteria.RATING, SortDirection.DESC, null, null, limit);

            Pageable pageable = PageRequest.of(0, limit);

            // when
            Page<BookDto> result = bookRepository.findBooks(condition, pageable);
            // then
            List<Double> ratings = result.getContent()
                    .stream()
                    .map(BookDto::rating)
                    .toList();

            assertTrue(ratings.get(0) > ratings.get(1));
        }

        @Test
        @DisplayName("[Success] 도서 목록 조회 - 1페이지 조회 후 cursor/after로 2페이지를 조회하면 중복 없이, 타이브레이커까지 검증하여 다음 페이지가 반환된다")
        void findBooks_cursorPagination_nextPage_should_success() throws InterruptedException {

            // given
            int limit = 10;
            int total = 25;
            String keyword = "Java";

            // title 정렬이 안정적으로 되도록 이름에 숫자 조합
            for (int i = 1; i <= total; i++) {
                String title = String.format("Java-%03d", i); // Java-001 ~ Java-025
                bookRepository.save(Book.create(
                        title,
                        "author",
                        "ISBN-" + i,
                        LocalDate.of(2026, 1, 1),
                        "pub",
                        null,
                        "desc"
                ));
            }

            // 타이브레이커 용도 같은 title 도서 저장
            bookRepository.save(Book.create("Java-010", "author", "ISBN-X1", LocalDate.of(2026, 1, 1), "pub", null, "desc"));
            sleep(5);
            bookRepository.save(Book.create("Java-010", "author", "ISBN-X2", LocalDate.of(2026, 1, 1), "pub", null, "desc"));

            em.flush();
            em.clear();

            Pageable pageable = PageRequest.of(0, limit);

            // when (1번째 페이지)
            BookSearchCondition firstCondition =
                    new BookSearchCondition(keyword, SortCriteria.TITLE, SortDirection.DESC, null, null, limit);

            Page<BookDto> firstPage = bookRepository.findBooks(firstCondition, pageable);

            // then (1번째 페이지 검증)
            assertEquals(limit, firstPage.getNumberOfElements());
            assertTrue(firstPage.hasNext());

            BookDto lastOfFirstPage = firstPage.getContent().get(firstPage.getNumberOfElements() - 1);

            // nextCursor/nextAfter 생성 (정렬 기준과 tie-breaker(createdAt) 규칙에 맞춰야 함)
            String nextCursor = lastOfFirstPage.title();               // TITLE 정렬이므로 title이 커서
            String nextAfter = lastOfFirstPage.createdAt().toString(); // 레포에서 parseAfter(String) 하니까 String으로 전달

            // when (2번째 페이지)
            BookSearchCondition secondCondition =
                    new BookSearchCondition(keyword, SortCriteria.TITLE, SortDirection.DESC, nextCursor, nextAfter, limit);

            Page<BookDto> secondPage = bookRepository.findBooks(secondCondition, pageable);

            // then (2번째 페이지 검증)
            assertEquals(limit, secondPage.getNumberOfElements());
            assertTrue(secondPage.hasNext()); // total=25면 2페이지(10개) 이후에도 남음(5개)

            // 중복 없음
            List<java.util.UUID> firstIds = firstPage.getContent().stream().map(BookDto::id).toList();
            List<java.util.UUID> secondIds = secondPage.getContent().stream().map(BookDto::id).toList();
            assertTrue(firstIds.stream().noneMatch(secondIds::contains));

            // 정렬 유지(2페이지 자체도 title desc)
            List<BookDto> secondContent = secondPage.getContent();
            Comparator<BookDto> sortRule =
                    Comparator.comparing(BookDto::title, Comparator.reverseOrder())
                            .thenComparing(BookDto::createdAt, Comparator.reverseOrder());

            assertThat(secondContent).isSortedAccordingTo(sortRule);

            // 타이브레이크 검증
            List<BookDto> java010 = secondContent.stream()
                    .filter(dto -> dto.title().equals("Java-010"))
                    .toList();

            assertThat(java010).hasSizeGreaterThanOrEqualTo(2);
            assertThat(java010)
                    .extracting(BookDto::createdAt)
                    .isSortedAccordingTo(Comparator.reverseOrder());

        }

        // TODO: 예외상황
    }

}
