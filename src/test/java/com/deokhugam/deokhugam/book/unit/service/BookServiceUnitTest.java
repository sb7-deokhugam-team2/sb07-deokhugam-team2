package com.deokhugam.deokhugam.book.unit.service;

import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.exception.BookNotFoundException;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.book.service.BookServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceUnitTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;


    @Nested
    @DisplayName("도서 id 단일 조회")
    class GetById {
        @Test
        @DisplayName("[Behavior][Positive] 단일 도서 조회 - bookRepository.findBookDetailById() 호출")
        void getById_should_delegate_and_return_result() {
            // given
            UUID bookId = UUID.randomUUID();
            BookDto bookDto = new BookDto(UUID.randomUUID(),
                    "title",
                   "author",
                    "description",
                    "publisher",
                    LocalDate.now(),
                    "1234567890123",
                    null,
                    0,
                    0.0,
                    Instant.now(),
                    Instant.now());
            when(bookRepository.findBookDetailById(bookId)).thenReturn(Optional.of(bookDto));
            // when
            BookDto result = bookService.getBookDetail(bookId);
            // then
            verify(bookRepository, times(1)).findBookDetailById(bookId);
            assertEquals(bookDto, result);
        }

        @Test
        @DisplayName("[Behavior][Negative] 단일 도서 조회 실패 - 존재하지 않으면 BookNotFoundException 발생")
        void getById_should_throw_BookNotFoundException_when_book_not_found() {
            // given
            UUID bookId = UUID.randomUUID();
            when(bookRepository.findBookDetailById(bookId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(
                    BookNotFoundException.class,
                    () -> bookService.getBookDetail(bookId)
            );

            verify(bookRepository, times(1)).findBookDetailById(bookId);
        }

    }

    @Nested
    @DisplayName("도서 커서기반 목록 조회")
    class SearchBooks {

        @Test
        @DisplayName("[Behavior][Positive] 목록 조회 - bookRepository.findBooks() 위임")
        void searchBooks_should_delegate_and_return_result() {

            // given
            BookSearchCondition condition = mock(BookSearchCondition.class);
            Page<BookDto> page = mock(Page.class);
            when(condition.limit()).thenReturn(10);
            when(bookRepository.findBooks(eq(condition), any(Pageable.class))).thenReturn(page);
            when(page.getContent()).thenReturn(List.of());
            when(page.hasNext()).thenReturn(false);
            when(page.getTotalElements()).thenReturn(0L);
            // when
            CursorPageResponseBookDto result = bookService.searchBooks(condition);

            // then
            verify(bookRepository, times(1)).findBooks(eq(condition), any(Pageable.class));

            assertEquals(0L, result.totalElements());
            assertFalse(result.hasNext());
            assertNull(result.nextCursor());
            assertNull(result.nextAfter());

        }

    }

}
