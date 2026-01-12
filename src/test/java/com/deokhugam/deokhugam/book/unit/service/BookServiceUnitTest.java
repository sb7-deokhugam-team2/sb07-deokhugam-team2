package com.deokhugam.deokhugam.book.unit.service;

import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.exception.BookNotFoundException;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.book.service.impl.BookServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceUnitTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;


    @Nested
    @DisplayName("도서 id 단일 조회")
    class getById {
        @Test
        @DisplayName("[Behavior][Positive] 단일 도서 조회 - bookRepository.findBookDetailById() 호출")
        void getById_should_delegate_and_return_result() {
            // given
            Book book = new Book("title", "author", "1234567890123", LocalDate.now(), "publisher", null, "description");
            BookDto bookDto = new BookDto(book.getId(), book.getTitle(), book.getAuthor(), book.getDescription(), book.getPublisher(), book.getPublishedDate(), book.getIsbn(), book.getThumbnailUrl(), 0, 0.0, book.getCreatedAt(), book.getUpdatedAt());
            when(bookRepository.findBookDetailById(book.getId())).thenReturn(Optional.of(bookDto));
            // when
            BookDto result = bookService.getBookDetail(book.getId());
            // then
            verify(bookRepository, times(1)).findBookDetailById(book.getId());
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

}
