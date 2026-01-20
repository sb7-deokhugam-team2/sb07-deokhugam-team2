package com.deokhugam.deokhugam.book.integration.service;

import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.exception.BookException;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.book.service.BookService;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.infrastructure.storage.FileStorage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@Tag("integration")
@ActiveProfiles("test")
public class BookServiceWorkflowTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @MockitoBean
    private FileStorage fileStorage;

    @AfterEach
    void tearDown() {
        // @Transactional이 없으므로 테스트가 끝나면 DB를 직접 비워줘야 다음 테스트에 영향이 없음
        bookRepository.deleteAll();
    }

    @Nested
    @DisplayName("CreateBook: 도서 생성")
    class CreateBook {

        @Nested
        @DisplayName("Positive Behavior (성공 케이스)")
        class PositiveBehavior {

            @Test
            @DisplayName("정상적인 요청과 썸네일 이미지가 주어지면 DB에 저장되고 S3 업로드가 호출된다")
            void createBook_success_with_image() {
                // given
                BookCreateRequest request = new BookCreateRequest(
                        "Integration Title",
                        "Author",
                        "Desc",
                        "Pub",
                        LocalDate.now(),
                        "9781000000001"
                );
                MockMultipartFile thumbnail = new MockMultipartFile(
                        "thumbnail",
                        "cover.jpg",
                        "image/jpeg",
                        "data".getBytes());

                given(fileStorage.generateUrl(anyString())).willReturn("http://mock-s3.com/books/cover.jpg");

                // when
                BookDto result = bookService.createBook(request, thumbnail);

                // then
                assertThat(result).isNotNull();
                assertThat(result.title()).isEqualTo(request.title());
                assertThat(result.thumbnailUrl()).isNotNull();

                boolean exists = bookRepository.existsByIsbn(request.isbn());
                assertThat(exists).isTrue();

                verify(fileStorage, times(1)).upload(any(MockMultipartFile.class), anyString());
                verify(fileStorage, never()).delete(anyString());
            }

            @Test
            @DisplayName("썸네일이 없어도 도서는 정상적으로 생성되어야 한다")
            void createBook_success_without_image() {
                // given
                BookCreateRequest request = new BookCreateRequest(
                        "No Image Book",
                        "Author",
                        "Desc",
                        "Pub",
                        LocalDate.now(),
                        "9781000000002"
                );

                // when
                BookDto result = bookService.createBook(request, null);

                // then
                assertThat(bookRepository.existsByIsbn(request.isbn())).isTrue();

                verify(fileStorage, never()).upload(any(), any());
            }

            @Test
            @DisplayName("책 생성 성공: DB 저장 확인 및 롤백용 파일 삭제가 호출되지 않아야 한다")
            void success_should_persist_and_keep_file() {
                // given
                BookCreateRequest request = new BookCreateRequest(
                        "Commit Test Book",
                        "Author",
                        "Desc",
                        "Pub",
                        LocalDate.now(),
                        "9781111111111"
                );
                MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "cover.jpg", "image/jpeg", "data".getBytes());

                given(fileStorage.generateUrl(anyString())).willReturn("http://fake-s3.com/cover.jpg");

                // when
                bookService.createBook(request, thumbnail);

                // then
                assertThat(bookRepository.existsByIsbn("9781111111111")).isTrue();

                verify(fileStorage, times(1)).upload(any(), anyString());

                verify(fileStorage, never()).delete(anyString());
            }
        }

        @Nested
        @DisplayName("Negative Behavior (실패 케이스)")
        class NegativeBehavior {

            @Test
            @DisplayName("이미 존재하는 ISBN으로 생성 시도 시 예외(DUPLICATE_BOOK_ISBN)가 발생한다")
            void createBook_fail_duplicate_isbn() {
                // given
                String isbn = "9781000000003";
                bookRepository.save(Book.create(
                        "Pre",
                        "Auth",
                        isbn,
                        LocalDate.now(),
                        "Pub",
                        null,
                        "Desc"));

                BookCreateRequest request = new BookCreateRequest(
                        "New Title",
                        "New Auth",
                        "Desc",
                        "New Pub",
                        LocalDate.now(),
                        isbn
                );

                // when & then
                assertThatThrownBy(() -> bookService.createBook(request, null))
                        .isInstanceOf(BookException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_BOOK_ISBN);
            }
        }
    }

    @Nested
    @DisplayName("UpdateBook (성공/커밋)")
    class UpdateBook {
        @Nested
        @DisplayName("Positive Behavior (성공 케이스)")
        class PositiveBehavior {

            @Test
            @DisplayName("정상적인 요청과 새로운 썸네일 이미지가 주어지면 DB가 수정되고 새 파일이 업로드된다")
            void updateBook_success_with_new_image() {
                // given
                String oldKey = "books/old-image.jpg";
                Book savedBook = bookRepository.save(Book.create(
                        "Old Title",
                        "Old Author",
                        "9782222222222",
                        LocalDate.now(),
                        "Old Pub",
                        oldKey,
                        "Old Desc"
                ));
                UUID bookId = savedBook.getId();

                BookUpdateRequest updateRequest = new BookUpdateRequest(
                        "New Title",
                        "New Author",
                        "New Desc",
                        "New Pub",
                        LocalDate.now()
                );
                MockMultipartFile newThumbnail = new MockMultipartFile("thumbnail", "new.jpg", "image/jpeg", "new-data".getBytes());

                given(fileStorage.generateUrl(anyString())).willReturn("http://mock-s3.com/books/new.jpg");

                // when
                BookDto result = bookService.updateBook(bookId, updateRequest, newThumbnail);

                // then
                assertThat(result).isNotNull();
                assertThat(result.title()).isEqualTo("New Title");
                assertThat(result.thumbnailUrl()).isEqualTo("http://mock-s3.com/books/new.jpg");

                Book updatedBook = bookRepository.findById(bookId).orElseThrow();
                assertThat(updatedBook.getTitle()).isEqualTo("New Title");
                assertThat(updatedBook.getThumbnailUrl()).isNotEqualTo(oldKey); // 키가 변경되었는지 확인
                assertThat(updatedBook.getThumbnailUrl()).contains("books/");   // 새 키 형식 확인

                verify(fileStorage, times(1)).upload(any(MockMultipartFile.class), anyString());
            }

            @Test
            @DisplayName("썸네일 파일 없이 요청하면 기존 이미지는 유지되고 메타데이터만 수정된다")
            void updateBook_success_data_only() {
                // given
                String originalKey = "books/keep-this.jpg";
                Book savedBook = bookRepository.save(Book.create(
                        "Original Title",
                        "Author",
                        "9783333333333",
                        LocalDate.now(),
                        "Pub",
                        originalKey,
                        "Desc"
                ));
                UUID bookId = savedBook.getId();

                BookUpdateRequest updateRequest = new BookUpdateRequest(
                        "Updated Title Only",
                        "Author",
                        "Desc",
                        "Pub",
                        LocalDate.now()
                );

                given(fileStorage.generateUrl(originalKey)).willReturn("http://mock-s3.com/books/keep-this.jpg");

                // when
                BookDto result = bookService.updateBook(bookId, updateRequest, null);

                // then
                assertThat(result.title()).isEqualTo("Updated Title Only");

                Book updatedBook = bookRepository.findById(bookId).orElseThrow();
                assertThat(updatedBook.getThumbnailUrl()).isEqualTo(originalKey);

                verify(fileStorage, never()).upload(any(), any());
            }

            @Test
            @DisplayName("책 수정 성공(커밋): 수정 후에도 기존 '구형 파일'은 삭제되지 않아야 한다")
            void success_should_keep_old_file_after_commit() {
                // given
                String oldKey = "books/must-delete.jpg";
                Book savedBook = bookRepository.save(Book.create(
                        "Title",
                        "Auth",
                        "9784444444444",
                        LocalDate.now(),
                        "Pub",
                        oldKey,
                        "Desc"
                ));

                BookUpdateRequest updateRequest = new BookUpdateRequest(
                        "New Title",
                        "New Auth",
                        "New Desc",
                        "New Pub",
                        LocalDate.now()
                );
                MockMultipartFile newThumbnail = new MockMultipartFile(
                        "thumbnail",
                        "new.jpg",
                        "image/jpeg",
                        "data".getBytes());

                given(fileStorage.generateUrl(anyString())).willReturn("http://mock-s3.com/new.jpg");

                // when
                bookService.updateBook(savedBook.getId(), updateRequest, newThumbnail);

                // then
                verify(fileStorage, times(1)).upload(any(), anyString());

                verify(fileStorage, times(1)).delete(oldKey);
            }

            @Nested
            @DisplayName("Negative Behavior (실패 케이스)")
            class NegativeBehavior {

                @Test
                @DisplayName("존재하지 않는 ID로 수정 시도 시 예외(BOOK_NOT_FOUND)가 발생한다")
                void updateBook_fail_not_found() {
                    // given
                    UUID nonExistentId = UUID.randomUUID();
                    BookUpdateRequest updateRequest = new BookUpdateRequest(
                            "Title", "Auth", "Desc", "Pub", LocalDate.now()
                    );

                    // when & then
                    assertThatThrownBy(() -> bookService.updateBook(nonExistentId, updateRequest, null))
                            .isInstanceOf(BookException.class) // 혹은 BookNotFoundException
                            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_NOT_FOUND);
                }

                @Test
                @DisplayName("삭제된(Soft Deleted) 책을 수정하려고 하면 예외(BOOK_NOT_FOUND)가 발생한다")
                void updateBook_fail_deleted_book() {
                    // given
                    Book savedBook = bookRepository.save(Book.create(
                            "Deleted Book", "Auth", "9785555555555", LocalDate.now(), "Pub", null, "Desc"
                    ));
                    savedBook.delete();
                    bookRepository.save(savedBook);

                    BookUpdateRequest updateRequest = new BookUpdateRequest(
                            "Try Update", "Auth", "Desc", "Pub", LocalDate.now()
                    );

                    // when & then
                    assertThatThrownBy(() -> bookService.updateBook(savedBook.getId(), updateRequest, null))
                            .isInstanceOf(BookException.class)
                            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_NOT_FOUND);
                }
            }

        }
    }
}
