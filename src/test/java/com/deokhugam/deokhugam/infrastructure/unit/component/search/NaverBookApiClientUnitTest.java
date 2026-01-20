package com.deokhugam.deokhugam.infrastructure.unit.component.search;

import com.deokhugam.infrastructure.search.book.NaverBookApiClient;
import com.deokhugam.infrastructure.search.book.dto.BookGlobalApiDto;
import com.deokhugam.infrastructure.search.book.dto.NaverApiResponse;
import com.deokhugam.infrastructure.search.book.exception.ApiConnectionException;
import com.deokhugam.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
public class NaverBookApiClientUnitTest {
    @InjectMocks
    private NaverBookApiClient naverBookApiClient;
    private MockRestServiceServer mockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    private final String CLIENT_ID = "test-client-id";
    private final String CLIENT_SECRET = "test-client-secret";
    private final String NAVER_URL = "https://openapi.naver.com/v1/search/book.json";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        naverBookApiClient = new NaverBookApiClient(restClient);

        setField(naverBookApiClient, "clientId", CLIENT_ID);
        setField(naverBookApiClient, "clientSecret", CLIENT_SECRET);
        setField(naverBookApiClient, "naverUrl", NAVER_URL);
    }

    @Nested
    @DisplayName("도서 데이터 조회 (GetData)")
    class GetData {

        @Test
        @DisplayName("[Behavior][Positive] 정상적인 응답 시 Header와 URL이 올바르게 설정되고 DTO가 반환되어야 한다")
        void getData_success() throws JsonProcessingException {
            // given
            String isbn = "9788912345678";
            String imageUrl = "http://image.url/test.jpg";
            byte[] mockImageBytes = new byte[]{1, 2, 3};

            Map<String, Object> itemMap = Map.of(
                    "title", "Test Title",
                    "author", "Author",
                    "description", "des",
                    "publisher", "Publisher",
                    "isbn", isbn,
                    "image", imageUrl,
                    "pubdate", "20230101"
            );
            String jsonResponse = objectMapper.writeValueAsString(Map.of("items", List.of(itemMap)));

            mockServer.expect(requestTo(containsString("query=" + isbn)))
                    .andExpect(method(HttpMethod.GET))
                    .andExpect(header("X-Naver-Client-Id", CLIENT_ID))
                    .andExpect(header("X-Naver-Client-Secret", CLIENT_SECRET))
                    .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

            mockServer.expect(requestTo(imageUrl))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(mockImageBytes, MediaType.APPLICATION_OCTET_STREAM));

            // when
            BookGlobalApiDto result = naverBookApiClient.getData(isbn);

            // then
            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("Test Title");
            assertThat(result.isbn()).isEqualTo(isbn);
            assertThat(result.publishedDate()).isEqualTo(LocalDate.of(2023, 1, 1));
            assertThat(result.thumbnailImage()).isEqualTo(mockImageBytes);

            mockServer.verify();
        }

        @Test
        @DisplayName("[Behavior][Negative] 네이버 API 호출 중 RestClientException 발생 시 ApiConnectionException을 던져야 한다")
        void getData_fail_api_connection() {
            // given
            String isbn = "9788912345678";

            mockServer.expect(requestTo(containsString("query=" + isbn)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withServerError());

            // when & then
            assertThatThrownBy(() -> naverBookApiClient.getData(isbn))
                    .isInstanceOf(ApiConnectionException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NAVER_API_CONNECTION_ERROR);

            mockServer.verify();
        }

        @Test
        @DisplayName("[Behavior][Positive] 날짜 형식이 잘못되었을 경우 예외를 던지지 않고 pubDate는 null이 되어야 한다")
        void getData_success_with_invalid_date() throws JsonProcessingException {
            // given
            String isbn = "9788912345678";
            String imageUrl = "http://image.url/test.jpg";

            // "pubdate" 형식이 잘못된 JSON 생성
            Map<String, Object> itemMap = Map.of(
                    "title", "Test Title",
                    "author", "Author",
                    "description", "des",
                    "publisher", "Publisher",
                    "isbn", isbn,
                    "image", imageUrl,
                    "pubdate", "INVALID-DATE" // 잘못된 날짜 형식
            );
            String jsonResponse = objectMapper.writeValueAsString(Map.of("items", List.of(itemMap)));

            mockServer.expect(requestTo(containsString("query=" + isbn)))
                    .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

            mockServer.expect(requestTo(imageUrl))
                    .andRespond(withSuccess(new byte[]{}, MediaType.APPLICATION_OCTET_STREAM));

            // when
            BookGlobalApiDto result = naverBookApiClient.getData(isbn);

            // then
            assertThat(result).isNotNull();
            assertThat(result.publishedDate()).isNull();

            mockServer.verify();
        }

        @Test
        @DisplayName("[Behavior][Positive] 이미지 다운로드 실패 시 예외를 던지지 않고 image는 null이 되어야 한다")
        void getData_success_with_image_download_fail() throws JsonProcessingException {
            // given
            String isbn = "9788912345678";
            String imageUrl = "http://fail.image/test.jpg";

            Map<String, Object> itemMap = Map.of(
                    "title", "Test Title",
                    "author", "Author",
                    "description", "des",
                    "publisher", "Publisher",
                    "isbn", isbn,
                    "image", imageUrl,
                    "pubdate", "20230101"
            );
            String jsonResponse = objectMapper.writeValueAsString(Map.of("items", List.of(itemMap)));

            mockServer.expect(requestTo(containsString("query=" + isbn)))
                    .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

            mockServer.expect(requestTo(imageUrl))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND));

            // when
            BookGlobalApiDto result = naverBookApiClient.getData(isbn);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isbn()).isEqualTo(isbn);
            assertThat(result.thumbnailImage()).isNull();

            mockServer.verify();
        }
    }
}
