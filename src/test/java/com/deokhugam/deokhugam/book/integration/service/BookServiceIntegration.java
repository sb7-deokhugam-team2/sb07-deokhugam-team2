package com.deokhugam.deokhugam.book.integration.service;

import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.enums.SortCriteria;
import com.deokhugam.domain.book.enums.SortDirection;
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

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;

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

}
