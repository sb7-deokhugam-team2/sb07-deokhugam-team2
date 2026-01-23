package com.deokhugam.deokhugam.book.integration.slice.web;

import com.deokhugam.domain.book.controller.BookController;
import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.dto.response.NaverBookDto;
import com.deokhugam.domain.book.enums.SortCriteria;
import com.deokhugam.domain.book.enums.SortDirection;
import com.deokhugam.domain.book.exception.BookException;
import com.deokhugam.domain.book.exception.BookNotFoundException;
import com.deokhugam.domain.book.service.BookService;
import com.deokhugam.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
public class BookControllerSliceTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    BookService bookService;

    private static final String ENDPOINT = "/api/books";

    @Nested
    @DisplayName("GET /api/books (커서 페이지네이션)")
    class SearchBooks {

        @Test
        @DisplayName("[200] 첫 페이지 조회: nextCursor/nextAfter/hasNext 응답 규격")
        void searchBooks_firstPage_should_return_nextCursor_nextAfter() throws Exception {
            // given
            Instant createdAt1 = Instant.parse("2026-01-14T06:37:05.110189Z");
            Instant createdAt2 = Instant.parse("2026-01-14T06:37:05.107722Z");

            BookDto bookDto1 = new BookDto(
                    UUID.randomUUID(),
                    "title-999",
                    "author",
                    "desc",
                    "publisher",
                    null,
                    "isbn",
                    null,
                    0L,
                    0.0,
                    createdAt1,
                    createdAt1
            );
            BookDto bookDto2 = new BookDto(
                    UUID.randomUUID(),
                    "title-998",
                    "author",
                    "desc",
                    "publisher",
                    null,
                    "isbn",
                    null,
                    0L,
                    0.0,
                    createdAt2,
                    createdAt2
            );

            CursorPageResponseBookDto response = new CursorPageResponseBookDto(
                    List.of(bookDto1, bookDto2),
                    bookDto2.title(),      // nextCursor는 "마지막 요소 기반"
                    bookDto2.createdAt(),  // nextAfter도 마지막 createdAt
                    2,
                    20L, // 임의로 20개라 가정하고 넣음
                    true // 임의 20개라면 hasNext가 true
            );

            when(bookService.searchBooks(any(BookSearchCondition.class))).thenReturn(response);

            // when & then
            mockMvc.perform(get(ENDPOINT)
                            .queryParam("orderBy", "TITLE")
                            .queryParam("direction", "DESC")
                            .queryParam("limit", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                    // 응답 필드 규격(스키마) 고정
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.nextCursor").value(bookDto2.title()))
                    .andExpect(jsonPath("$.nextAfter").value(bookDto2.createdAt().toString()))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.totalElements").value(20))
                    .andExpect(jsonPath("$.hasNext").value(true));

            // 컨트롤러가 BookSearchCondition을 제대로 만들어 서비스에 넘겼는지 확인
            ArgumentCaptor<BookSearchCondition> captor = ArgumentCaptor.forClass(BookSearchCondition.class);
            verify(bookService, times(1)).searchBooks(captor.capture());

            BookSearchCondition sent = captor.getValue();
            assertThat(sent.orderBy()).isEqualTo(SortCriteria.TITLE);
            assertThat(sent.direction()).isEqualTo(SortDirection.DESC);
            assertThat(sent.limit()).isEqualTo(10);
        }

        @Test
        @DisplayName("[200] 2번째 페이지 요청: cursor/after 전달시  응답 스키마 유지")
        void searchBooks_secondPage_should_pass_cursor_after() throws Exception {
            // given
            String cursor = "title-900";
            String after = "2026-01-14T06:37:05.100000Z";

            CursorPageResponseBookDto response = new CursorPageResponseBookDto(
                    List.of(),
                    null,
                    null,
                    0,
                    20L,
                    false
            );
            when(bookService.searchBooks(any(BookSearchCondition.class))).thenReturn(response);

            // when & then
            mockMvc.perform(get(ENDPOINT)
                            .queryParam("keyword", "Java")
                            .queryParam("orderBy", "TITLE")
                            .queryParam("direction", "DESC")
                            .queryParam("cursor", cursor)
                            .queryParam("after", after)
                            .queryParam("limit", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.nextCursor").doesNotExist()) // null이면 Jackson 설정에 따라 없을 수도/있을 수도
                    .andExpect(jsonPath("$.nextAfter").doesNotExist())
                    .andExpect(jsonPath("$.hasNext").value(false));

            ArgumentCaptor<BookSearchCondition> captor = ArgumentCaptor.forClass(BookSearchCondition.class);
            verify(bookService).searchBooks(captor.capture());

            BookSearchCondition sent = captor.getValue();
            assertThat(sent.keyword()).isEqualTo("Java");
            assertThat(sent.cursor()).isEqualTo(cursor);
            assertThat(sent.after()).isEqualTo(after);
        }

        @Test
        @DisplayName("[400] after 파싱 불가: 400 + 에러 응답 규격(최소한 status/message)")
        void searchBooks_invalid_after_should_return_400() throws Exception {
            // given
            String invalidAfter = "NOT_A_DATE_TIME";

            // when & then
            mockMvc.perform(get(ENDPOINT)
                            .queryParam("orderBy", "TITLE")
                            .queryParam("direction", "DESC")
                            .queryParam("after", invalidAfter)
                            .queryParam("limit", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").exists());

            verify(bookService, never()).searchBooks(any());
        }

        @Test
        @DisplayName("[200] limit이 0 이하이면 기본값(50)으로 보정되어 서비스에 전달된다")
        void searchBooks_invalid_limit_should_default_to_50() throws Exception {

            // given
            when(bookService.searchBooks(any())).thenReturn(
                    new CursorPageResponseBookDto(List.of(), null, null, 0, 0L, false)
            );


            // when & then
            mockMvc.perform(get(ENDPOINT)
                            .queryParam("limit", "0")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            ArgumentCaptor<BookSearchCondition> captor = ArgumentCaptor.forClass(BookSearchCondition.class);
            verify(bookService).searchBooks(captor.capture());

            assertThat(captor.getValue().limit()).isEqualTo(50);
        }

        @Test
        @DisplayName("[400] orderBy 파라미터가 잘못됐을때 400 status (orderBy=WRONG)")
        void searchBooks_invalid_enum_should_return_400() throws Exception {
            mockMvc.perform(get(ENDPOINT)
                            .queryParam("orderBy", "WRONG")
                            .queryParam("direction", "DESC")
                            .queryParam("limit", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).searchBooks(any());
        }
    }

    @Nested
    @DisplayName("POST /api/books (도서 생성)")
    class CreateBook {

        @Test
        @DisplayName("[201] 정상 요청: 이미지와 JSON 데이터가 모두 유효하면 도서가 생성된다")
        void createBook_success_with_image() throws Exception {
            // given
            BookCreateRequest request = new BookCreateRequest(
                    "Clean Code",
                    "Robert C. Martin",
                    "A code of conduct for professional programmers",
                    "Insight",
                    LocalDate.of(2013, 12, 24),
                    "9788966260959"

            );

            String jsonContent = objectMapper.writeValueAsString(request);
            MockMultipartFile bookDataFile = new MockMultipartFile(
                    "bookData",
                    "",
                    "application/json",
                    jsonContent.getBytes(StandardCharsets.UTF_8)
            );

            MockMultipartFile thumbnailFile = new MockMultipartFile(
                    "thumbnailImage",
                    "cover.jpg",
                    "image/jpeg",
                    "dummy-image-content".getBytes()
            );

            // 3. Expected Response
            BookDto responseDto = new BookDto(
                    UUID.randomUUID(),
                    request.title(),
                    request.author(),
                    request.description(),
                    request.publisher(),
                    request.publishedDate(),
                    request.isbn(),
                    "http://s3.url/books/cover.jpg",
                    0L,
                    0.0,
                    Instant.now(),
                    Instant.now()
            );

            given(bookService.createBook(any(BookCreateRequest.class), any())).willReturn(responseDto);

            // when & then
            mockMvc.perform(multipart(ENDPOINT)
                            .file(bookDataFile)
                            .file(thumbnailFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.title").value(request.title()))
                    .andExpect(jsonPath("$.isbn").value(request.isbn()))
                    .andExpect(jsonPath("$.thumbnailUrl").value("http://s3.url/books/cover.jpg"));

            ArgumentCaptor<BookCreateRequest> reqCaptor = ArgumentCaptor.forClass(BookCreateRequest.class);
            ArgumentCaptor<MockMultipartFile> fileCaptor = ArgumentCaptor.forClass(MockMultipartFile.class);

            verify(bookService, times(1)).createBook(reqCaptor.capture(), fileCaptor.capture());

            BookCreateRequest capturedRequest = reqCaptor.getValue();
            assertThat(capturedRequest.title()).isEqualTo(request.title());
            assertThat(capturedRequest.isbn()).isEqualTo(request.isbn());

            MockMultipartFile capturedFile = fileCaptor.getValue();
            assertThat(capturedFile.getOriginalFilename()).isEqualTo("cover.jpg");
        }

        @Test
        @DisplayName("[201] 정상 요청: 이미지가 없어도(null) 도서 생성은 성공해야 한다")
        void createBook_success_without_image() throws Exception {
            // given
            BookCreateRequest request = new BookCreateRequest(
                    "No Image Book",
                    "Author",
                    "Description",
                    "Publisher",
                    LocalDate.now(),
                    "9780000000001"
            );

            MockMultipartFile bookDataFile = new MockMultipartFile(
                    "bookData",
                    "",
                    "application/json",
                    objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
            );

            BookDto responseDto = new BookDto(
                    UUID.randomUUID(),
                    request.title(),
                    request.author(),
                    request.description(),
                    request.publisher(),
                    request.publishedDate(),
                    request.isbn(),
                    null, // 썸네일 없음
                    0L, 0.0,
                    Instant.now(), Instant.now()
            );

            given(bookService.createBook(any(BookCreateRequest.class), any())).willReturn(responseDto);

            // when & then
            // thumbnailImage 파트를 아예 보내지 않음
            mockMvc.perform(multipart(ENDPOINT)
                            .file(bookDataFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value(request.title()))
                    .andExpect(jsonPath("$.thumbnailUrl").doesNotExist()); // null 체크

            verify(bookService).createBook(any(BookCreateRequest.class), any()); // 두 번째 인자는 null 혹은 empty file
        }

        @Test
        @DisplayName("[400] 필수 파트 누락: bookData(JSON) 파트가 없으면 400 에러")
        void createBook_fail_missing_part() throws Exception {
            // given
            MockMultipartFile thumbnailFile = new MockMultipartFile(
                    "thumbnailImage", "cover.jpg", "image/jpeg", "content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart(ENDPOINT)
                            .file(thumbnailFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest());

            verify(bookService, times(0)).createBook(any(), any());
        }

        @Test
        @DisplayName("[409] 중복 ISBN: 서비스에서 예외 발생 시 적절한 에러 응답(Conflict 등)을 반환해야 한다")
        void createBook_fail_duplicate_isbn() throws Exception {
            // given
            BookCreateRequest request = new BookCreateRequest(
                    "Duplicate Book",
                    "Author",
                    "Desc",
                    "Pub",
                    LocalDate.now(),
                    "9781111111111"
            );

            MockMultipartFile bookDataFile = new MockMultipartFile(
                    "bookData", "", "application/json",
                    objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
            );

            // Service가 중복 예외를 던지도록 설정
            given(bookService.createBook(any(), any()))
                    .willThrow(new BookException(ErrorCode.DUPLICATE_BOOK_ISBN));

            // when & then
            mockMvc.perform(multipart(ENDPOINT)
                            .file(bookDataFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print())
                    .andExpect(status().is4xxClientError())
                    .andExpect(jsonPath("$.message").exists());
        }
    }


    @Nested
    @DisplayName("PATCH /api/books/{bookId} (도서 수정)")
    class UpdateBook {

        @Test
        @DisplayName("[200] 정상 요청: 이미지와 변경 데이터가 모두 있으면 도서 정보와 썸네일이 수정된다")
        void updateBook_success_with_image() throws Exception {
            // given
            UUID bookId = UUID.randomUUID();
            BookUpdateRequest updateRequest = new BookUpdateRequest(
                    "Updated Title",
                    "Updated Author",
                    "Updated Publisher",
                    "Updated Description",
                    LocalDate.now()

            );

            MockMultipartFile bookDataFile = new MockMultipartFile(
                    "bookData",
                    "",
                    "application/json",
                    objectMapper.writeValueAsString(updateRequest).getBytes(StandardCharsets.UTF_8)
            );

            MockMultipartFile thumbnailFile = new MockMultipartFile(
                    "thumbnailImage",
                    "new-cover.jpg",
                    "image/jpeg",
                    "new-image-content".getBytes()
            );

            BookDto responseDto = new BookDto(
                    bookId,
                    updateRequest.title(),
                    updateRequest.author(),
                    updateRequest.description(),
                    updateRequest.publisher(),
                    updateRequest.publishedDate(),
                    "9788966260959",
                    "http://s3.url/books/new-cover.jpg",
                    10L, 4.5,
                    Instant.now().minusSeconds(999999),
                    Instant.now()
            );

            given(bookService.updateBook(eq(bookId), any(BookUpdateRequest.class), any())).willReturn(responseDto);

            // when & then
            mockMvc.perform(multipart(ENDPOINT + "/{bookId}", bookId)
                            .file(bookDataFile)
                            .file(thumbnailFile)
                            .with(request -> {
                                request.setMethod("PATCH"); // ⭐️ 핵심: HTTP 메서드를 강제로 PATCH로 변경
                                return request;
                            })
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(bookId.toString()))
                    .andExpect(jsonPath("$.title").value(updateRequest.title()))
                    .andExpect(jsonPath("$.thumbnailUrl").value("http://s3.url/books/new-cover.jpg"));

            // Verify
            verify(bookService).updateBook(eq(bookId), any(BookUpdateRequest.class), any());
        }

        @Test
        @DisplayName("[200] 정상 요청: 썸네일 파일 없이 메타데이터만 수정할 수도 있다")
        void updateBook_success_data_only() throws Exception {
            // given
            UUID bookId = UUID.randomUUID();
            BookUpdateRequest updateRequest = new BookUpdateRequest(
                    "Title Only",
                    "Author",
                    "Desc",
                    "Pub",
                    LocalDate.now()
            );

            MockMultipartFile bookDataFile = new MockMultipartFile(
                    "bookData", "", "application/json",
                    objectMapper.writeValueAsString(updateRequest).getBytes(StandardCharsets.UTF_8)
            );

            BookDto responseDto = new BookDto(
                    bookId, updateRequest.title(), updateRequest.author(), updateRequest.description(),
                    updateRequest.publisher(), updateRequest.publishedDate(), "isbn",
                    "http://original.url/cover.jpg",
                    0L, 0.0, Instant.now(), Instant.now()
            );

            given(bookService.updateBook(eq(bookId), any(BookUpdateRequest.class), any())).willReturn(responseDto);

            // when & then
            mockMvc.perform(multipart(ENDPOINT + "/{bookId}", bookId)
                            .file(bookDataFile)
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            })
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Title Only"));


            verify(bookService).updateBook(eq(bookId), any(BookUpdateRequest.class), or(isNull(), any(MultipartFile.class)));
        }

        @Test
        @DisplayName("[404] 존재하지 않는 도서: 수정 시도 시 404 에러를 반환해야 한다")
        void updateBook_fail_not_found() throws Exception {
            // given
            UUID nonExistentId = UUID.randomUUID();
            BookUpdateRequest updateRequest = new BookUpdateRequest(
                    "T",
                    "A",
                    "D",
                    "P",
                    LocalDate.now()
            );

            MockMultipartFile bookDataFile = new MockMultipartFile(
                    "bookData", "", "application/json",
                    objectMapper.writeValueAsString(updateRequest).getBytes(StandardCharsets.UTF_8)
            );

            given(bookService.updateBook(eq(nonExistentId), any(), any()))
                    .willThrow(new BookNotFoundException(ErrorCode.BOOK_NOT_FOUND));

            // when & then
            mockMvc.perform(multipart(ENDPOINT + "/{bookId}", nonExistentId)
                            .file(bookDataFile)
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            })
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/books/info (네이버 책 정보 조회)")
    class GetBookInfoByIsbn {

        @Test
        @DisplayName("[200] 유효한 ISBN으로 요청 시 네이버 도서 정보를 반환한다")
        void getBookInfoByIsbn_success() throws Exception {
            // given
            String isbn = "9788966260959";
            byte[] mockImageBytes = "fake-image-byte-content".getBytes(StandardCharsets.UTF_8);
            NaverBookDto mockResponse = new NaverBookDto(
                    "Clean Code",
                    "Robert C. Martin",
                    "A Handbook of Agile Software Craftsmanship",
                    "Insight",
                    LocalDate.of(2013, 12, 24),
                    isbn,
                    mockImageBytes
            );

            given(bookService.getBookByIsbn(isbn)).willReturn(mockResponse);

            // when & then
            mockMvc.perform(get(ENDPOINT + "/info")
                            .queryParam("isbn", isbn) // ?isbn=...
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Clean Code"))
                    .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                    .andExpect(jsonPath("$.isbn").value(isbn))
                    .andExpect(jsonPath("$.thumbnailImage").value(java.util.Base64.getEncoder().encodeToString(mockImageBytes)));

            verify(bookService).getBookByIsbn(isbn);
        }

        @Test
        @DisplayName("[404] 네이버에 존재하지 않는 ISBN 요청 시 404 에러를 반환한다")
        void getBookInfoByIsbn_not_found() throws Exception {
            // given
            String invalidIsbn = "9780000000000";

            given(bookService.getBookByIsbn(invalidIsbn))
                    .willThrow(new com.deokhugam.domain.book.exception.BookISBNNotFoundException(ErrorCode.BOOK_NOT_FOUND));

            // when & then
            mockMvc.perform(get(ENDPOINT + "/info")
                            .queryParam("isbn", invalidIsbn)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound()) // 404 Not Found
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("[400] ISBN 파라미터 누락 시 400 에러를 반환한다")
        void getBookInfoByIsbn_missing_param() throws Exception {
            // given

            // when & then
            mockMvc.perform(get(ENDPOINT + "/info")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(bookService, times(0)).getBookByIsbn(any());
        }
    }

    @Nested
    @DisplayName("POST /api/books/isbn/ocr (이미지 ISBN 추출)")
    class ExtractIsbnFromImage {

        @Test
        @DisplayName("[200] 유효한 이미지 업로드 시 추출된 ISBN 문자열을 반환한다")
        void extractIsbn_success() throws Exception {
            // given
            String expectedIsbn = "9788966260959";

            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "barcode.jpg",
                    "image/jpeg",
                    "fake-image-binary".getBytes()
            );

            given(bookService.extractIsbnFromImage(any(MultipartFile.class)))
                    .willReturn(expectedIsbn);

            // when & then
            mockMvc.perform(multipart(ENDPOINT + "/isbn/ocr")
                            .file(imageFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string(expectedIsbn));

            verify(bookService).extractIsbnFromImage(any(MultipartFile.class));
        }

        @Test
        @DisplayName("[400] 이미지 파일 파트('image')가 누락되면 400 에러를 반환한다")
        void extractIsbn_fail_missing_part() throws Exception {
            // given
            // 파일 없이 요청

            // when & then
            mockMvc.perform(multipart(ENDPOINT + "/isbn/ocr")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest());

            verify(bookService, times(0)).extractIsbnFromImage(any());
        }

        @Test
        @DisplayName("[400] 빈 파일 업로드 시 서비스 예외(OCRFileEmptyException)가 발생하고 400을 반환한다")
        void extractIsbn_fail_empty_file() throws Exception {
            // given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "image", "empty.jpg", "image/jpeg", new byte[0]
            );

            given(bookService.extractIsbnFromImage(any()))
                    .willThrow(new com.deokhugam.infrastructure.ocr.exception.OCRFileEmptyException(ErrorCode.OCR_EMPTY_FILE_EXCEPTION));

            // when & then
            mockMvc.perform(multipart(ENDPOINT + "/isbn/ocr")
                            .file(emptyFile))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("[500] 외부 API 연동 실패 시(OCRConnectionException) 500 에러를 반환한다")
        void extractIsbn_fail_api_error() throws Exception {
            // given
            MockMultipartFile imageFile = new MockMultipartFile(
                    "image", "barcode.jpg", "image/jpeg", "content".getBytes()
            );

            given(bookService.extractIsbnFromImage(any()))
                    .willThrow(new com.deokhugam.infrastructure.ocr.exception.OCRConnectionException(ErrorCode.OCR_API_CONNECTION_EXCEPTION));

            // when & then
            mockMvc.perform(multipart(ENDPOINT + "/isbn/ocr")
                            .file(imageFile))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("DELETE  /api/books (도서 물리 삭제)")
    class HardDeleteBook{
        @Test
        @DisplayName("[204] 도서 물리 삭제 - 204 No Content 반환")
        void hardDeleteBook_success() throws Exception {
            // given
            UUID bookId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete("/api/books/hard/{bookId}", bookId))
                    .andExpect(status().isNoContent());

            // 서비스 호출 검증
            verify(bookService).hardDeleteBook(bookId);
        }

        @Test
        @DisplayName("[Fail] 도서 물리 삭제 - 존재하지 않는 도서면 404 반환")
        void hardDeleteBook_notFound() throws Exception {
            UUID bookId = UUID.randomUUID();

            doThrow(new BookNotFoundException(ErrorCode.BOOK_NOT_FOUND)) // 반환값없는 메서드 doThrow
                    .when(bookService).hardDeleteBook(bookId);

            mockMvc.perform(delete("/api/books/hard/{bookId}", bookId))
                    .andExpect(status().isNotFound());
        }
    }
}
