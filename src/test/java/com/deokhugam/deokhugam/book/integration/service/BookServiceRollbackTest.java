package com.deokhugam.deokhugam.book.integration.service;

import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.book.service.BookService;
import com.deokhugam.infrastructure.storage.FileStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class BookServiceTransactionTest {

    @Autowired
    private BookService bookService;

    @MockitoBean
    private BookRepository mockBookRepository;

    @MockitoBean
    private FileStorage fileStorage;

    @Nested
    @DisplayName("CreateBook: 생성 트랜잭션 관리")
    class CreateBook {

        @Test
        @DisplayName("[Rollback] 생성 중 DB 예외 발생 시 -> 롤백되어 업로드한 파일이 삭제되어야 한다")
        void create_fail_rollback_should_delete_uploaded_file() {
            // given
            BookCreateRequest request = new BookCreateRequest(
                    "Rollback Title", "Auth", "Desc", "Pub", LocalDate.now(), "9781000000001"
            );
            MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "new.jpg", "image/jpeg", "data".getBytes());

            given(fileStorage.generateUrl(anyString())).willReturn("http://fake.com/new.jpg");

            given(mockBookRepository.save(any())).willThrow(new RuntimeException("DB Error"));

            // when
            try {
                bookService.createBook(request, thumbnail);
            } catch (RuntimeException e) {
                // ignore exception
            }

            // then
            verify(fileStorage, times(1)).upload(any(), anyString());

            verify(fileStorage, times(1)).delete(anyString());
        }
    }

    @Nested
    @DisplayName("UpdateBook: 수정 트랜잭션 관리")
    class UpdateBook {

        @Test
        @DisplayName("[Commit] 수정 성공 시 -> 트랜잭션 커밋 후 '구형 파일'이 삭제되어야 한다")
        void update_success_commit_should_delete_old_file() {
            // given
            UUID bookId = UUID.randomUUID();
            String oldKey = "books/old-image.jpg";

            Book mockBook = mock(Book.class);
            given(mockBook.getId()).willReturn(bookId);
            given(mockBook.getThumbnailUrl()).willReturn(oldKey);
            given(mockBook.isDeleted()).willReturn(false);

            BookDto mockBookDetail = mock(BookDto.class);
            given(mockBookDetail.reviewCount()).willReturn(5L);
            given(mockBookDetail.rating()).willReturn(3.5);

            given(mockBookRepository.findById(any(UUID.class))).willReturn(Optional.of(mockBook));
            given(mockBookRepository.findBookDetailById(any())).willReturn(Optional.of(mockBookDetail));
            given(fileStorage.generateUrl(anyString())).willReturn("http://fake.com/new.jpg");

            BookUpdateRequest updateRequest = new BookUpdateRequest("New", "Auth", "Desc", "Pub", LocalDate.now());
            MockMultipartFile newThumbnail = new MockMultipartFile("thumbnail", "new.jpg", "image/jpeg", "data".getBytes());

            // when
            bookService.updateBook(bookId, updateRequest, newThumbnail);

            // then
            verify(fileStorage, times(1)).upload(any(), anyString());

            verify(fileStorage, times(1)).delete(oldKey);
        }

        @Test
        @DisplayName("[Rollback] 수정 중 예외 발생 시 -> 롤백되어 업로드한 '새 파일'이 삭제되어야 한다")
        void update_fail_rollback_should_delete_new_file() {
            // given
            UUID bookId = UUID.randomUUID();

            // Mock 엔티티 설정
            Book mockBook = mock(Book.class);
            given(mockBook.isDeleted()).willReturn(false);
            given(mockBook.getThumbnailUrl()).willReturn("books/old-image.jpg");

            given(mockBookRepository.findById(bookId)).willReturn(Optional.of(mockBook));
            given(fileStorage.generateUrl(anyString())).willReturn("http://fake.com/new.jpg");

            BookUpdateRequest updateRequest = new BookUpdateRequest("New", "Auth", "Desc", "Pub", LocalDate.now());
            MockMultipartFile newThumbnail = new MockMultipartFile("thumbnail", "new-rollback.jpg", "image/jpeg", "data".getBytes());

            willThrow(new RuntimeException("Update Logic Failed"))
                    .given(mockBook)
                    .update(any(), any(), any(), any(), any(), anyString());

            // when
            try {
                bookService.updateBook(bookId, updateRequest, newThumbnail);
            } catch (RuntimeException e) {
                // ignore
            }

            // then
            verify(fileStorage, times(1)).upload(any(), anyString());

            verify(fileStorage, times(1)).delete(anyString());
        }
    }
}
