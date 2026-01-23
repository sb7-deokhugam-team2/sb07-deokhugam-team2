package com.deokhugam.deokhugam.book.unit.service;

import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.exception.BookException;
import com.deokhugam.domain.book.exception.BookNotFoundException;
import com.deokhugam.domain.book.mapper.BookUrlMapper;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.book.service.BookServiceImpl;
import com.deokhugam.domain.popularbook.dto.response.CursorResult;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.infrastructure.storage.FileStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceUnitTest {

    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private FileStorage s3Storage;

    @Mock
    private BookUrlMapper bookUrlMapper;

    private Book createPersistedBook(UUID id) {
        Book book = Book.create("Test Title", "Author", "11111",
                LocalDate.now(), "Publisher", null, "Desc");
        ReflectionTestUtils.setField(book, "id", id);
        Instant now = Instant.now();
        ReflectionTestUtils.setField(book, "createdAt", now);
        ReflectionTestUtils.setField(book, "updatedAt", now);

        return book;
    }

    private BookCreateRequest createRequest(String isbn) {
        return new BookCreateRequest("Test Title", "Author", "Desc",
                "Publisher", LocalDate.now(), isbn);
    }

    private BookUpdateRequest updateRequest() {
        return new BookUpdateRequest("Test Title", "Author", "Desc",
                "Publisher", LocalDate.now());
    }

    @Nested
    @DisplayName("도서 생성")
    class CreateBook {

        @Test
        @DisplayName("[Behavior][Positive] 도서 생성 성공 - 썸네일이 있는 경우 랜덤 키 생성 및 업로드 수행")
        void createBook_Success_WithThumbnail() {
            // given
            String isbn = "11111";
            UUID generatedId = UUID.randomUUID();

            BookCreateRequest request = createRequest(isbn);
            MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "test.jpg", "image/jpeg", "content".getBytes());
            Book persistedBook = createPersistedBook(generatedId);

            given(bookRepository.existsByIsbn(isbn)).willReturn(false);

            String expectedRandomKey = "books/random-uuid.jpg";
            given(s3Storage.upload(any(MockMultipartFile.class), anyString()))
                    .willReturn(expectedRandomKey);

            given(bookRepository.save(any(Book.class))).willReturn(persistedBook);

            String expectedUrl = "https://cdn.com/" + expectedRandomKey;
            given(s3Storage.generateUrl(anyString())).willReturn(expectedUrl);

            // when
            BookDto result = bookService.createBook(request, thumbnail);

            // then
            assertThat(result.isbn()).isEqualTo(isbn);
            assertThat(result.thumbnailUrl()).isEqualTo(expectedUrl);

            // Verify
            verify(s3Storage).upload(any(MockMultipartFile.class), anyString());
            verify(bookRepository, times(1)).save(any(Book.class));
        }

        @Test
        @DisplayName("[Behavior][Negative] 책 생성 실패 - ISBN 중복 시 예외 발생, 업로드 및 저장은 호출되지 않음")
        void createBook_Fail_DuplicateIsbn() {
            // given
            String duplicateIsbn = "99999";
            BookCreateRequest request = createRequest(duplicateIsbn);

            given(bookRepository.existsByIsbn(duplicateIsbn)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> bookService.createBook(request, null))
                    .isInstanceOf(BookException.class)
                    .hasMessageContaining(ErrorCode.DUPLICATE_BOOK_ISBN.getMessage());

            // verify
            verify(bookRepository).existsByIsbn(duplicateIsbn);
            verify(bookRepository, never()).save(any(Book.class));
            verify(s3Storage, never()).upload(any(), anyString());
        }
    }

    @Nested
    @DisplayName("도서 수정")
    class UpdateBook {
        @Test
        @DisplayName("[Behavior][Positive] 도서 수정 성공 - 썸네일 변경 시 새로운 랜덤 키로 업로드 및 엔티티 갱신")
        void updateBook_Success_WithNewThumbnail() {
            // given
            UUID bookId = UUID.randomUUID();
            BookUpdateRequest request = updateRequest();

            Book existingBook = createPersistedBook(bookId);
            String oldS3Key = "books/old-uuid.jpg";
            ReflectionTestUtils.setField(existingBook, "thumbnailUrl", oldS3Key);

            MockMultipartFile newThumbnail = new MockMultipartFile("thumbnail", "new.png", "image/png", "new_content".getBytes());

            given(bookRepository.findById(bookId)).willReturn(Optional.of(existingBook));

            BookDto dummyDto = new BookDto(bookId, "Title", "Author", "11111",
                    "pub", LocalDate.now(), "Desc", "url", 0L, 0.0, Instant.now(), Instant.now());

            given(bookRepository.findBookDetailById(bookId)).willReturn(Optional.of(dummyDto));
            given(s3Storage.upload(any(MockMultipartFile.class), anyString()))
                    .willAnswer(invocation -> invocation.getArgument(1));

            String expectedNewUrl = "https://cdn.com/new-random-key.png";
            given(s3Storage.generateUrl(anyString())).willReturn(expectedNewUrl);

            //when
            BookDto result = bookService.updateBook(bookId, request, newThumbnail);

            // then
            verify(s3Storage).upload(any(MockMultipartFile.class), anyString());

            assertThat(existingBook.getThumbnailUrl()).isNotEqualTo(oldS3Key);
            assertThat(existingBook.getThumbnailUrl()).startsWith("books/");
            assertThat(existingBook.getThumbnailUrl()).endsWith(".png");
        }

        @Test
        @DisplayName("[Behavior][Positive] 도서 수정 성공 - 썸네일 미첨부 시 기존 이미지 URL 유지")
        void updateBook_Success_NoThumbnail_KeepsOriginalUrl() {
            // given
            UUID bookId = UUID.randomUUID();
            BookUpdateRequest request = updateRequest();

            Book existingBook = createPersistedBook(bookId);
            String oldS3Key = "books/original.jpg";
            ReflectionTestUtils.setField(existingBook, "thumbnailUrl", oldS3Key);

            given(bookRepository.findById(bookId)).willReturn(Optional.of(existingBook));

            BookDto dummyDto = new BookDto(bookId, "Title", "Author", "11111",
                    "pub", LocalDate.now(), "Desc", "url", 0L, 0.0, Instant.now(), Instant.now());
            given(bookRepository.findBookDetailById(bookId)).willReturn(Optional.of(dummyDto));

            given(s3Storage.generateUrl(oldS3Key)).willReturn("https://cdn.com/" + oldS3Key);

            // when
            bookService.updateBook(bookId, request, null);

            // then
            verify(s3Storage, never()).upload(any(), anyString());

            assertThat(existingBook.getThumbnailUrl()).isEqualTo(oldS3Key);
        }

        @Test
        @DisplayName("[Behavior][Negative] 도서 수정 실패 - 존재하지 않는 도서 ID")
        void updateBook_Fail_BookNotFound() {
            // given
            UUID nonExistentId = UUID.randomUUID();
            BookUpdateRequest request = updateRequest();

            given(bookRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bookService.updateBook(nonExistentId, request, null))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessageContaining(ErrorCode.BOOK_NOT_FOUND.getMessage());
        }
    }


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
            when(bookUrlMapper.withFullThumbnailUrl(any(BookDto.class))).thenReturn(bookDto);
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
            CursorResult<BookDto> page = mock(CursorResult.class);
            when(condition.limit()).thenReturn(10);
            when(bookRepository.findBooks(eq(condition), any(Pageable.class))).thenReturn(page);
            when(page.content()).thenReturn(List.of());
            when(page.hasNext()).thenReturn(false);
            when(page.total()).thenReturn(0L);
            when(bookUrlMapper.withFullThumbnailUrl(anyList())).thenReturn(List.of());
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

    @Nested
    @DisplayName("도서 논리 삭제")
    class SoftDelete {
        @Test
        @DisplayName("[Behavior][Positive] 도서 논리삭제 - 있는 book Id에 대해서 book.delete() 위힘")
        void softDelete_should_delegate_delete() {
            // given
            UUID uuid = UUID.randomUUID();
            Book book = mock(Book.class);
            when(bookRepository.findById(uuid)).thenReturn(Optional.of(book));
            // when
            bookService.softDeleteBook(uuid);
            // then
            verify(bookRepository, times(1)).findById(uuid);
            verify(book, times(1)).delete();
        }

        @Test
        @DisplayName("[Behavior][Negative] 도서 논리삭제 - 해당 book Id의 book이 없을시 BookNotFoundException 위임 및 book.delete() 위임 안됨")
        void softDelete_should_throw_BookNotFoundException_when_book_not_found() {
            // given
            UUID uuid = UUID.randomUUID();
            when(bookRepository.findById(uuid)).thenReturn(Optional.empty());

            // when
            assertThrows(BookNotFoundException.class, () -> bookService.softDeleteBook(uuid));

            // then
            verify(bookRepository, times(1)).findById(uuid);
        }
    }

    @Nested
    @DisplayName("도서 물리 삭제")
    class HardDelete {
        @Test
        @DisplayName("[behavior][Positive] 도서 물리삭제 - 물리 삭제 bookRepository.deleteById 위임")
        void hardDelete_should_delegate_delete() {
            // given
            UUID uuid = UUID.randomUUID();
            Book book = mock(Book.class);
            when(bookRepository.existsById(uuid)).thenReturn(true);

            // when
            bookService.hardDeleteBook(uuid);

            // then
            verify(bookRepository, times(1)).existsById(uuid);
            verify(bookRepository, times(1)).deleteById(uuid);
        }

        @Test
        @DisplayName("[Propagation][Negative] 도서 물리삭제 - 해당 book 존재 하지않을시 BookNotFound 예외전파")
        void hardDelete_should_throws_BookNotFoundException_when_notFound() {
            // given
            UUID uuid = UUID.randomUUID();
            when(bookRepository.existsById(uuid)).thenReturn(false);

            // when & then
            assertThrows(BookNotFoundException.class, () -> bookService.hardDeleteBook(uuid));
        }
    }

}
