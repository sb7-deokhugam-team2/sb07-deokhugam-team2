package com.deokhugam.deokhugam.book.unit.service;

import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.exception.BookException;
import com.deokhugam.domain.book.exception.BookNotFoundException;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.book.service.BookServiceImpl;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.global.storage.FileStorage;
import com.deokhugam.global.storage.exception.S3.S3FileStorageException;
import jakarta.transaction.Transaction;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceUnitTest {

    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private FileStorage s3Storage;

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

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

    @Nested
    @DisplayName("도서 생성")
    class createBook {
        @Test
        @DisplayName("[Behavior][Positive] 도서 생성 성공 - 썸네일이 있는 경우 업로드 및 URL 생성 수행")
        void createBook_Success_WithThumbnail(){
            //given
            String isbn = "11111";
            UUID generatedId = UUID.randomUUID();

            BookCreateRequest request = createRequest(isbn);
            MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "test.jpg", "image/jpeg", "content".getBytes());

            Book persistedBook = createPersistedBook(generatedId);;

            given(bookRepository.save(any(Book.class))).willReturn(persistedBook);

            given(bookRepository.existsByIsbn(isbn)).willReturn(false);

            String expectedS3Key = "books/" + generatedId + ".jpg";
            given(s3Storage.upload(any(MockMultipartFile.class), eq(generatedId.toString())))
                    .willReturn(expectedS3Key);

            String expectedUrl = "https://cdn.com/" + expectedS3Key;
            given(s3Storage.generateUrl(anyString())).willReturn(expectedUrl);

            //when
            BookDto result = bookService.createBook(request, thumbnail);

            //then
            assertThat(result.isbn()).isEqualTo(isbn);
            assertThat(result.thumbnailUrl()).isEqualTo(expectedUrl);

            verify(s3Storage).upload(any(MockMultipartFile.class), eq(generatedId.toString()));
            verify(bookRepository, times(2)).save(any(Book.class));

            assertThat(persistedBook.getThumbnailUrl()).contains(expectedS3Key);
            assertThat(persistedBook.getThumbnailUrl()).contains("?v=");
        }

        @Test
        @DisplayName("[Behavior][Negative] 책 생성 실패 - 저장 후 ISBN 중복 확인 시 예외 발생 및 업로드 미수행")
        void createBook_Fail_DuplicateIsbn(){
            //given
            String duplicateIsbn = "99999";
            UUID generatedId = UUID.randomUUID();
            BookCreateRequest request = createRequest(duplicateIsbn);


            Book persistedBook = createPersistedBook(generatedId);
            given(bookRepository.save(any(Book.class))).willReturn(persistedBook);

            given(bookRepository.existsByIsbn(duplicateIsbn)).willReturn(true);
            //when & then
            assertThatThrownBy(() -> bookService.createBook(request, null))
                    .isInstanceOf(BookException.class)
                    .hasMessageContaining(ErrorCode.DUPLICATE_BOOK_ISBN.getMessage());

            // verify
            verify(bookRepository, times(1)).save(any(Book.class));

            verify(bookRepository).existsByIsbn(duplicateIsbn);

            verify(s3Storage, never()).upload(any(), anyString());
        }
    }

    @Nested
    @DisplayName("도서 수정")
    class UpdateBook {
        @Test
        @DisplayName("[Behavior][Positive] 도서 수정 성공 - 확장자가 변경된 경우 기존 파일 삭제 수행 (jpg -> png)")
        void updateBook_should_delete_old_file_when_extension_changed(){
            //given
            UUID bookId = UUID.randomUUID();
            BookCreateRequest request = createRequest("11111");

            Book existingBook = createPersistedBook(bookId);
            String oldS3Key = "books/" + bookId + ".jpg";
            existingBook.updateThumbnailUrl(oldS3Key + "?v=" + LocalDate.from(Instant.now()));

            MockMultipartFile newThumbnail = new MockMultipartFile("thumbnail", "new_image.png", "image/png", "contents".getBytes());

            given(bookRepository.findById(bookId)).willReturn(Optional.of(existingBook));
            given(bookRepository.existsByIsbn(anyString())).willReturn(false);

            String newS3Key = "books/" + bookId + ".png";
            given(s3Storage.upload(any(), anyString())).willReturn(newS3Key);

            BookDto dummyDto = mock(BookDto.class);

            given(bookRepository.findBookDetailById(bookId)).willReturn(Optional.of(dummyDto));
            given(s3Storage.generateUrl(anyString())).willReturn("https://cdn.com/" + newS3Key);

            //when
            bookService.updateBook(bookId, request, newThumbnail);

            //then
            verify(s3Storage).upload(any(MockMultipartFile.class), eq(newS3Key));
            verify(s3Storage).delete(eq(oldS3Key));

            assertThat(existingBook.getThumbnailUrl()).startsWith(newS3Key);
        }
        @Test
        @DisplayName("[Behavior][Positive] 도서 수정 성공 - 확장자가 동일한 경우 기존 파일 삭제 안 함 (jpg -> jpg)")
        void updateBook_should_not_delete_old_file_when_extension_same(){
            //given
            UUID bookId = UUID.randomUUID();
            BookCreateRequest request = createRequest("11111");

            Book existingBook = createPersistedBook(bookId);
            String oldS3Key = "books/" + bookId + ".jpg";
            existingBook.updateThumbnailUrl(oldS3Key + "?v=" + Instant.now());

            MockMultipartFile newThumbnail = new MockMultipartFile("thumbnail", "new.jpg", "image/jpeg", "contents".getBytes());

            given(bookRepository.findById(bookId)).willReturn(Optional.of(existingBook));
            given(s3Storage.upload(any(), anyString())).willReturn(oldS3Key);

            given(bookRepository.findBookDetailById(bookId)).willReturn(Optional.of(mock(BookDto.class)));
            given(s3Storage.generateUrl(anyString())).willReturn("https://cdn.com/" + oldS3Key);

            //when
            bookService.updateBook(bookId, request, newThumbnail);

            //then
            verify(s3Storage).upload(any(MockMultipartFile.class), eq(oldS3Key));
            verify(s3Storage, never()).delete(anyString());
        }

        @Test
        @DisplayName("[Behavior][Positive] 도서 수정 성공 - 기존 파일 삭제가 실패해도 트랜잭션은 완료되어야 함 (예외 무시)")
        void updateBook_should_complete_transaction_even_if_delete_fails() {
            // given
            UUID bookId = UUID.randomUUID();
            BookCreateRequest request = createRequest("11111");

            Book existingBook = createPersistedBook(bookId);
            String oldS3Key = "books/" + bookId + ".jpg";
            existingBook.updateThumbnailUrl(oldS3Key + "?v=" + Instant.now());

            MockMultipartFile newThumbnail = new MockMultipartFile("thumbnail", "new.png", "image/png", "contents".getBytes());

            given(bookRepository.findById(bookId)).willReturn(Optional.of(existingBook));
            given(bookRepository.existsByIsbn(anyString())).willReturn(false);

            String newS3Key = "books/" + bookId + ".png";
            given(s3Storage.upload(any(), anyString())).willReturn(newS3Key);

            given(bookRepository.findBookDetailById(bookId)).willReturn(Optional.of(mock(BookDto.class)));
            given(s3Storage.generateUrl(anyString())).willReturn("https://cdn.com/" + newS3Key);

            doThrow(new S3FileStorageException(ErrorCode.FAIL_TO_DELETE_FILE))
                    .when(s3Storage).delete(eq(oldS3Key));

            BookDto result = bookService.updateBook(bookId, request, newThumbnail);

            // then
            assertThat(result).isNotNull();
            verify(s3Storage).delete(eq(oldS3Key));
            verify(s3Storage).upload(any(), eq(newS3Key));
        }


        @Test
        @DisplayName("[Behavior][Negative] 도서 수정 실패 - 존재하지 않는 도서 ID")
        void updateBook_should_throw_exception_when_book_not_found() {
            // given
            UUID nonExistentId = UUID.randomUUID();
            BookCreateRequest request = createRequest("11111");

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
