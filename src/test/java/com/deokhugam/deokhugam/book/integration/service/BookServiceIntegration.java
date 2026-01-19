package com.deokhugam.deokhugam.book.integration.service;

import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.enums.SortCriteria;
import com.deokhugam.domain.book.enums.SortDirection;
import com.deokhugam.domain.book.exception.BookNotFoundException;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.book.service.BookService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class BookServiceIntegration {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;
    
    @Autowired
    EntityManager em;

    @Nested
    class SearchBooks {

        @Test
        @DisplayName("[Positive] 도서 목록 조회 - 첫페이지 조회시 cursor기반 응답값 nextCursor, nextAfter 제공")
        public void searchBooks_firstPage_should_return_nextCursor_nextAfter_success() throws InterruptedException {

            // given
            for (int i = 0; i < 20; i++) {
                bookRepository.save(Book.create(String.format("title-%03d", i),
                        "author-" + i,
                        "isbn-" + i,
                        LocalDate.now(),
                        "publisher-" + i,
                        null,
                        "desc-" + i));
                sleep(5);
            }
            em.flush();
            em.clear();

            BookSearchCondition condition = new BookSearchCondition(null, SortCriteria.TITLE, SortDirection.DESC, null, null, 10);

            // when
            CursorPageResponseBookDto cursorPageResponseBookDto = bookService.searchBooks(condition);
            List<BookDto> content = cursorPageResponseBookDto.content();
            assertThat(content).isNotEmpty(); // 가정 명시
            BookDto lastDto = content.get(content.size() - 1);

            // then
            assertThat(cursorPageResponseBookDto.hasNext()).isTrue();
            assertThat(lastDto.title()).isEqualTo(cursorPageResponseBookDto.nextCursor());
            assertThat(lastDto.createdAt()).isEqualTo(cursorPageResponseBookDto.nextAfter());

        }
    }

    @Nested
    @DisplayName("도서 논리삭제")
    class SoftDelete {
        @Test
        @DisplayName("[Positive] 도서 논리 삭제 - 논리 삭제시 isDeleted가 true 변경")
        void softDeleteBook_should_change_isDeleted_true() {
            // given
            Book book = Book.create("title", "author", "isbn", LocalDate.now(), "publisher", null, "desc");
            bookRepository.save(book);
            UUID bookId = book.getId();
            em.flush();
            em.clear();

            // when
            bookService.softDeleteBook(bookId);
            em.flush();
            em.clear();
            Book foundBook = bookRepository.findById(bookId).orElseThrow();
            // then
            assertThat(foundBook.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("[Propagation][Negative] 도서 논리 삭제 -  해당 book id의 book이 DB에하는게 없을 시 BookNotFoundException 예외전파")
        void softDeleteBook_should_throw_BookNotFoundException_when_notFound() {
            // given
            UUID invalidBookId = UUID.randomUUID();

            // when & then
            assertThrows(BookNotFoundException.class, () -> bookService.softDeleteBook(invalidBookId));
        }
    }

    @Nested
    @DisplayName("도서 물리삭제")
    class HardDelete {
        @Test
        @DisplayName("[Positive] 도서 물리 삭제 - 삭제 후 findById는 empty")
        void hardDeleteBook_should_return_empty_book() {
            // given
            Book book = Book.create("title", "author", "isbn", LocalDate.now(), "publisher", null, "desc");
            bookRepository.save(book);
            UUID bookId = book.getId();
            em.flush();
            em.clear();
            assertThat(bookRepository.findById(bookId)).isPresent(); // 가정 명시
            // when
            bookService.hardDeleteBook(bookId);
            em.flush();
            em.clear();

            // then
            assertThat(bookRepository.findById(bookId)).isEmpty();
        }

        @Test
        @DisplayName("[Negative] 물리 삭제 - 해당 book id 없을시 BookNotFound 예외전파")
        void hard_delete_should_throw_BookNotFoundException_when_notFound() {
            // given
            UUID invalidBookId = UUID.randomUUID();

            // when & then
            assertThrows(BookNotFoundException.class, () -> bookService.hardDeleteBook(invalidBookId));
        }
    }

}
