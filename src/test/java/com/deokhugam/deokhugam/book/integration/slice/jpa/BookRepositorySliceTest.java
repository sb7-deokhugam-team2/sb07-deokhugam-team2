package com.deokhugam.deokhugam.book.integration.slice.jpa;

import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
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
import org.springframework.data.domain.Slice;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
@DisplayName("BookRepository JPA Slice Test")
public class BookRepositorySliceTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    EntityManager em;

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
        void findBooks_should_success() {

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
            Slice<BookDto> sliceBooks = bookRepository.findBooks(condition, pageable);

            // then
            assertFalse(sliceBooks.getContent().isEmpty());
            assertEquals(limit, sliceBooks.getNumberOfElements());
            assertTrue(sliceBooks.hasNext());

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
            Slice<BookDto> sliceBooks = bookRepository.findBooks(condition, pageable);

            // then
            assertFalse(sliceBooks.getContent().isEmpty());
            assertEquals(savedCount, sliceBooks.getNumberOfElements());
            assertFalse(sliceBooks.hasNext());

            // (선택) 실제 저장 개수와 일치 sanity check
            assertEquals(savedCount, sliceBooks.getContent().size());
        }

    }

}
