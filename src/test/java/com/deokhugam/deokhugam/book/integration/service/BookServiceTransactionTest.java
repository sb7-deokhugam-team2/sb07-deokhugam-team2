package com.deokhugam.deokhugam.book.integration.service;

import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.mapper.BookUrlMapper;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.book.service.BookService;
import com.deokhugam.infrastructure.storage.FileStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class BookServiceTransactionTest {

    @Autowired
    private BookService bookService;

    @MockitoBean
    private BookRepository bookRepository;

    @MockitoBean
    private FileStorage fileStorage;

    @MockitoBean
    private BookUrlMapper bookUrlMapper;

    @Nested
    @DisplayName("CreateBook: 생성 트랜잭션 (신규 생성 & 복구)")
    class CreateBook {

        @Test
        @DisplayName("[Rollback] 신규 생성 중 DB 예외 발생 -> 롤백되어 '업로드한 새 파일'이 삭제되어야 한다")
        void create_fail_rollback_should_delete_uploaded_file() {
            // given
            String isbn = "9781000000001";
            BookCreateRequest request = createCreateRequest(isbn);
            MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "new.jpg", "image/jpeg", "data".getBytes());

            given(bookRepository.findByIsbn(isbn)).willReturn(Optional.empty());

            given(bookRepository.save(any(Book.class))).willThrow(new RuntimeException("DB Connection Error"));

            // when
            try {
                bookService.createBook(request, thumbnail);
            } catch (RuntimeException e) {
                // ignore
            }

            // then
            verify(fileStorage, times(1)).upload(any(), anyString());

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(fileStorage, times(1)).delete(keyCaptor.capture());

            String deletedKey = keyCaptor.getValue();
            assertThat(deletedKey).startsWith("books/").endsWith(".jpg");
        }

        @Test
        @DisplayName("[Commit] 삭제된 도서 복구 성공 -> 커밋 후 '기존 구형 파일'이 삭제되어야 한다")
        void restore_success_commit_should_delete_old_file() {
            // given
            String isbn = "9781000000002";
            BookCreateRequest request = createCreateRequest(isbn);
            MockMultipartFile newThumbnail = new MockMultipartFile("thumbnail", "restore.jpg", "image/jpeg", "data".getBytes());

            String oldKey = "books/old-deleted.jpg";
            Book deletedBook = createBookEntity(UUID.randomUUID(), oldKey, true); // isDeleted=true

            given(bookRepository.findByIsbn(isbn)).willReturn(Optional.of(deletedBook));

            given(bookRepository.findBookDetailById(any())).willReturn(Optional.of(mock(BookDto.class)));
            given(bookUrlMapper.withFullThumbnailUrl(any(BookDto.class))).willReturn(mock(BookDto.class));

            // when
            bookService.createBook(request, newThumbnail);

            // then
            verify(fileStorage).upload(any(), anyString());

            verify(fileStorage).delete(oldKey);
        }
    }

    @Nested
    @DisplayName("UpdateBook: 수정 트랜잭션")
    class UpdateBook {

        @Test
        @DisplayName("[Commit] 수정 성공 -> 커밋 후 '구형 파일'이 삭제되어야 한다")
        void update_success_commit_should_delete_old_file() {
            // given
            UUID bookId = UUID.randomUUID();
            String oldKey = "books/old-image.jpg";

            Book existingBook = createBookEntity(bookId, oldKey, false);

            given(bookRepository.findById(bookId)).willReturn(Optional.of(existingBook));
            given(bookRepository.findBookDetailById(bookId)).willReturn(Optional.of(mock(BookDto.class)));
            given(bookUrlMapper.withFullThumbnailUrl(any(BookDto.class))).willReturn(mock(BookDto.class));

            BookUpdateRequest updateRequest = new BookUpdateRequest("New Title", "Auth", "Desc", "Pub", LocalDate.now());
            MockMultipartFile newThumbnail = new MockMultipartFile("thumbnail", "update.jpg", "image/jpeg", "data".getBytes());

            // when
            bookService.updateBook(bookId, updateRequest, newThumbnail);

            // then
            verify(fileStorage).upload(any(), anyString());

            verify(fileStorage).delete(oldKey);
        }

        @Test
        @DisplayName("[Rollback] 수정 중 예외 발생 -> 롤백되어 '업로드한 새 파일'이 삭제되어야 한다")
        void update_fail_rollback_should_delete_new_file() {
            // given
            UUID bookId = UUID.randomUUID();
            String oldKey = "books/original.jpg";

            Book existingBook = createBookEntity(bookId, oldKey, false);

            given(bookRepository.findById(bookId)).willReturn(Optional.of(existingBook));

            given(bookRepository.findBookDetailById(bookId)).willThrow(new RuntimeException("Error during DTO conversion"));

            BookUpdateRequest updateRequest = new BookUpdateRequest("New", "Auth", "Desc", "Pub", LocalDate.now());
            MockMultipartFile newThumbnail = new MockMultipartFile("thumbnail", "new-rollback.jpg", "image/jpeg", "data".getBytes());

            // when
            try {
                bookService.updateBook(bookId, updateRequest, newThumbnail);
            } catch (RuntimeException e) {
                // ignore
            }

            // then
            verify(fileStorage, times(1)).upload(any(), anyString());

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(fileStorage).delete(keyCaptor.capture());

            String deletedKey = keyCaptor.getValue();

            assertThat(deletedKey).isNotEqualTo(oldKey);
            assertThat(deletedKey).startsWith("books/");
        }
    }

    // --- Helper Methods ---

    private BookCreateRequest createCreateRequest(String isbn) {
        return new BookCreateRequest("Title", "Auth", "Desc", "Pub", LocalDate.now(), isbn);
    }

    private Book createBookEntity(UUID id, String thumbnailUrl, boolean isDeleted) {
        Book book = Book.create("Old Title", "Old Auth", "123", LocalDate.now(), "Old Pub", thumbnailUrl, "Old Desc");
        ReflectionTestUtils.setField(book, "id", id);

        if (isDeleted) {
            book.delete();
        }
        return book;
    }
}