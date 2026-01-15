package com.deokhugam.deokhugam.book.integration.slice.web;

import com.deokhugam.domain.book.controller.BookController;
import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.enums.SortCriteria;
import com.deokhugam.domain.book.enums.SortDirection;
import com.deokhugam.domain.book.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

}
