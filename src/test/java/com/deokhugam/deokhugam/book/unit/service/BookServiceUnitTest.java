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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

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
    private FileStorage fileStorage;

    @Mock
    private BookUrlMapper bookUrlMapper;

    private Book createPersistedBook(UUID id, String isbn, boolean isDeleted) {
        Book book = Book.create("Original Title", "Author", isbn,
                LocalDate.now(), "Publisher", null, "Desc");

        ReflectionTestUtils.setField(book, "id", id);

        if (isDeleted) {
            book.delete();
        }

        Instant now = Instant.now();
        ReflectionTestUtils.setField(book, "createdAt", now);
        ReflectionTestUtils.setField(book, "updatedAt", now);

        return book;
    }

    private BookCreateRequest createRequest(String isbn) {
        return new BookCreateRequest("New Title", "New Author", "New Desc",
                "New Publisher", LocalDate.now(), isbn);
    }

    private BookUpdateRequest updateRequest() {
        return new BookUpdateRequest("Updated Title", "Updated Author", "Updated Desc",
                "Updated Publisher", LocalDate.now());
    }

    private BookDto createBookDto(UUID id, String isbn, String fullUrl) {
        return new BookDto(
                id, "Title", "Author", "Desc", "Publisher", LocalDate.now(),
                isbn, fullUrl, 0L, 0.0, Instant.now(), Instant.now()
        );
    }

    @Nested
    @DisplayName("도서 생성 (CreateBook)")

    class CreateBook {

        @Test
        @DisplayName("[Success] 신규 도서 생성 - 썸네일 파일이 있는 경우 업로드 후 저장")
        void createBook_Success_NewBook_WithThumbnail() {
            // given
            String isbn = "11111";
            BookCreateRequest request = createRequest(isbn);
            MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "test.jpg", "image/jpeg", "content".getBytes());

            given(bookRepository.findByIsbn(isbn)).willReturn(Optional.empty());

            Book savedBook = createPersistedBook(UUID.randomUUID(), isbn, false);
            given(bookRepository.save(any(Book.class))).willReturn(savedBook);

            String expectedFullUrl = "https://cdn.com/books/random-uuid.jpg";
            BookDto expectedDto = createBookDto(savedBook.getId(), isbn, expectedFullUrl);
            given(bookUrlMapper.withFullThumbnailUrl(any(BookDto.class))).willReturn(expectedDto);

            // when
            BookDto result = bookService.createBook(request, thumbnail);

            // then
            assertThat(result.isbn()).isEqualTo(isbn);
            assertThat(result.thumbnailUrl()).isEqualTo(expectedFullUrl);

            // Verify
            verify(fileStorage).upload(any(MockMultipartFile.class), anyString());
            verify(bookRepository).save(any(Book.class));
            verify(bookUrlMapper).withFullThumbnailUrl(any(BookDto.class));
        }

        @Test
        @DisplayName("[Success] 신규 도서 생성 - 썸네일이 없는 경우 이미지 없이 저장")
        void createBook_Success_NewBook_NoThumbnail() {
            // given
            String isbn = "22222";
            BookCreateRequest request = createRequest(isbn);

            given(bookRepository.findByIsbn(isbn)).willReturn(Optional.empty());

            Book savedBook = createPersistedBook(UUID.randomUUID(), isbn, false);
            given(bookRepository.save(any(Book.class))).willReturn(savedBook);

            BookDto expectedDto = createBookDto(savedBook.getId(), isbn, null);
            given(bookUrlMapper.withFullThumbnailUrl(any(BookDto.class))).willReturn(expectedDto);

            // when
            BookDto result = bookService.createBook(request, null);

            // then
            assertThat(result.thumbnailUrl()).isNull();
            verify(fileStorage, never()).upload(any(), anyString());
        }

        @Test
        @DisplayName("[Success] 삭제된 도서 복구 (Restore) - 기존 정보 업데이트 및 파일 교체")
        void createBook_Success_RestoreDeletedBook() {
            // given
            String isbn = "33333";
            BookCreateRequest request = createRequest(isbn);
            MockMultipartFile newThumbnail = new MockMultipartFile("thumbnail", "new.jpg", "image/jpeg", "new".getBytes());

            Book deletedBook = createPersistedBook(UUID.randomUUID(), isbn, true); // isDeleted = true
            ReflectionTestUtils.setField(deletedBook, "thumbnailUrl", "books/old.jpg");

            given(bookRepository.findByIsbn(isbn)).willReturn(Optional.of(deletedBook));
            String newFullUrl = "https://cdn.com/books/new-uuid.jpg";
            BookDto restoreResultDto = createBookDto(deletedBook.getId(), isbn, newFullUrl);

            given(bookRepository.findBookDetailById(deletedBook.getId())).willReturn(Optional.of(restoreResultDto));
            given(bookUrlMapper.withFullThumbnailUrl(any(BookDto.class))).willReturn(restoreResultDto);

            // when
            BookDto result = bookService.createBook(request, newThumbnail);

            // then
            assertThat(deletedBook.isDeleted()).isFalse();
            assertThat(deletedBook.getTitle()).isEqualTo(request.title());

            // verify
            verify(fileStorage).upload(any(MultipartFile.class), anyString());
        }

        @Test
        @DisplayName("[Fail] 도서 생성 실패 - 이미 존재하는 ISBN (삭제되지 않음)")
        void createBook_Fail_DuplicateIsbn() {
            // given
            String duplicateIsbn = "99999";
            BookCreateRequest request = createRequest(duplicateIsbn);

            Book activeBook = createPersistedBook(UUID.randomUUID(), duplicateIsbn, false);
            given(bookRepository.findByIsbn(duplicateIsbn)).willReturn(Optional.of(activeBook));

            // when & then
            assertThatThrownBy(() -> bookService.createBook(request, null))
                    .isInstanceOf(BookException.class)
                    .hasMessageContaining(ErrorCode.DUPLICATE_BOOK_ISBN.getMessage());

            // verify
            verify(bookRepository, never()).save(any(Book.class));
            verify(fileStorage, never()).upload(any(), anyString());
        }
    }

    @Nested
    @DisplayName("도서 수정 (UpdateBook)")
    class UpdateBook {
        @Test
        @DisplayName("[Success] 도서 수정 성공 - 썸네일 변경 시 업로드 및 엔티티 업데이트")
        void updateBook_Success_WithNewThumbnail() {
            // given
            UUID bookId = UUID.randomUUID();
            BookUpdateRequest request = updateRequest();
            MockMultipartFile newThumbnail = new MockMultipartFile("thumbnail", "update.png", "image/png", "update".getBytes());

            Book existingBook = createPersistedBook(bookId, "11111", false);
            String oldKey = "books/old.jpg";
            ReflectionTestUtils.setField(existingBook, "thumbnailUrl", oldKey);

            given(bookRepository.findById(bookId)).willReturn(Optional.of(existingBook));

            BookDto updatedDto = createBookDto(bookId, "11111", "https://cdn.com/books/new.png");
            given(bookRepository.findBookDetailById(bookId)).willReturn(Optional.of(updatedDto));
            given(bookUrlMapper.withFullThumbnailUrl(updatedDto)).willReturn(updatedDto);

            // when
            BookDto result = bookService.updateBook(bookId, request, newThumbnail);

            // then
            verify(fileStorage).upload(any(MultipartFile.class), anyString());
            assertThat(existingBook.getThumbnailUrl()).isNotEqualTo(oldKey);
            assertThat(existingBook.getTitle()).isEqualTo(request.title()); // 필드 변경 확인
        }

        @Test
        @DisplayName("[Success] 도서 수정 성공 - 썸네일 미첨부 시 기존 URL 유지")
        void updateBook_Success_NoThumbnail_KeepsOriginalUrl() {
            // given
            UUID bookId = UUID.randomUUID();
            BookUpdateRequest request = updateRequest();

            Book existingBook = createPersistedBook(bookId, "11111", false);
            String originalKey = "books/original.jpg";
            ReflectionTestUtils.setField(existingBook, "thumbnailUrl", originalKey);

            given(bookRepository.findById(bookId)).willReturn(Optional.of(existingBook));

            BookDto updatedDto = createBookDto(bookId, "11111", "https://cdn.com/books/original.jpg");
            given(bookRepository.findBookDetailById(bookId)).willReturn(Optional.of(updatedDto));
            given(bookUrlMapper.withFullThumbnailUrl(updatedDto)).willReturn(updatedDto);

            // when
            bookService.updateBook(bookId, request, null);

            // then
            verify(fileStorage, never()).upload(any(), anyString());
            assertThat(existingBook.getThumbnailUrl()).isEqualTo(originalKey); // 키 유지 확인
        }

        @Test
        @DisplayName("[Fail] 도서 수정 실패 - 존재하지 않는 도서 ID")
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

        @Test
        @DisplayName("[Fail] 도서 수정 실패 - 삭제된(Soft Deleted) 도서")
        void updateBook_Fail_AlreadyDeleted() {
            // given
            UUID bookId = UUID.randomUUID();
            BookUpdateRequest request = updateRequest();

            Book deletedBook = createPersistedBook(bookId, "99999", true);
            given(bookRepository.findById(bookId)).willReturn(Optional.of(deletedBook));

            // when & then
            assertThatThrownBy(() -> bookService.updateBook(bookId, request, null))
                    .isInstanceOf(BookNotFoundException.class) // 로직상 NotFound 던짐
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
            when(bookRepository.findBooks(eq(condition))).thenReturn(page);
            when(page.content()).thenReturn(List.of());
            when(page.hasNext()).thenReturn(false);
            when(page.total()).thenReturn(0L);
            when(bookUrlMapper.withFullThumbnailUrl(anyList())).thenReturn(List.of());
            // when
            CursorPageResponseBookDto result = bookService.searchBooks(condition);

            // then
            verify(bookRepository, times(1)).findBooks(eq(condition));

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
