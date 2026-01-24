package com.deokhugam.deokhugam.infrastructure.unit.component.ocr;

import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.infrastructure.ocr.OCRApiClient;
import com.deokhugam.infrastructure.ocr.dto.OCRResponse;
import com.deokhugam.infrastructure.ocr.exception.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class OCRApiClientUnitTest {
    private OCRApiClient ocrApiClient;
    private MockRestServiceServer mockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String API_KEY = "test-ocr-key";
    private final String OCR_URL = "https://api.ocr.space/parse/image";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        ocrApiClient = new OCRApiClient(restClient);

        setField(ocrApiClient, "ocrApiKey", API_KEY);
    }

    @Nested
    @DisplayName("ISBN 추출 (ExtractIsbn)")
    class ExtractIsbn {

        @Test
        @DisplayName("[Behavior][Positive] 정상적인 이미지 파일 전송 시 ISBN이 추출되어야 한다")
        void extractIsbn_success() throws JsonProcessingException {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", "dummy-content".getBytes()
            );
            String expectedIsbn = "9788966260959";
            String extractedText = "Text with ISBN: " + expectedIsbn;

            OCRResponse.ParsedResult parsedResult = new OCRResponse.ParsedResult(
                    extractedText,
                    ""
            );

            OCRResponse mockResponseDto = new OCRResponse(
                    List.of(parsedResult),
                    "",
                    false
            );
            String jsonResponse = objectMapper.writeValueAsString(mockResponseDto);

            mockServer.expect(requestTo(OCR_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(header("apikey", API_KEY))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                    .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

            // when
            String result = ocrApiClient.extractIsbn(file);

            // then
            assertThat(result).isEqualTo(expectedIsbn);
            mockServer.verify();
        }

        @Test
        @DisplayName("[Behavior][Negative] 파일이 비어있을 경우 예외를 던져야 한다")
        void extractIsbn_fail_empty_file() {
            // given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "empty.jpg", "image/jpeg", new byte[0]
            );

            // when & then (API 호출 전 검증)
            assertThatThrownBy(() -> ocrApiClient.extractIsbn(emptyFile))
                    .isInstanceOf(OCRFileEmptyException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OCR_EMPTY_FILE_EXCEPTION);
        }

        @Test
        @DisplayName("[Behavior][Negative] API 서버 에러(500) 발생 시 예외를 던져야 한다")
        void extractIsbn_fail_connection_error() {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", "content".getBytes()
            );

            mockServer.expect(requestTo(OCR_URL))
                    .andRespond(withServerError());

            // when & then
            assertThatThrownBy(() -> ocrApiClient.extractIsbn(file))
                    .isInstanceOf(OCRConnectionException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OCR_API_CONNECTION_EXCEPTION);

            mockServer.verify();
        }

        @Test
        @DisplayName("[Behavior][Negative] OCR 결과에 에러 메시지가 포함된 경우 예외를 던져야 한다")
        void extractIsbn_fail_api_internal_error() throws JsonProcessingException {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", "content".getBytes()
            );

            OCRResponse.ParsedResult parsedResult = new OCRResponse.ParsedResult(
                    "parsedResults",
                    "errorMessage"
            );

            OCRResponse errorResponse = new OCRResponse(
                    List.of(parsedResult),
                    "File invalid",
                    true
            );

            String jsonResponse = objectMapper.writeValueAsString(errorResponse);

            mockServer.expect(requestTo(OCR_URL))
                    .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

            // when & then
            assertThatThrownBy(() -> ocrApiClient.extractIsbn(file))
                    .isInstanceOf(OCRInternalException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OCR_API_INTERNAL_ERROR);

            mockServer.verify();
        }

        @Test
        @DisplayName("[Behavior][Positive] 추출된 텍스트에 ISBN 패턴이 없을 경우 null을 반환해야 한다")
        void extractIsbn_success_no_isbn_found() throws JsonProcessingException {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", "content".getBytes()
            );

            OCRResponse.ParsedResult parsedResult = new OCRResponse.ParsedResult(
                    "No numbers here just text",
                    ""
            );

            OCRResponse responseMap = new OCRResponse(
                    List.of(parsedResult),
                    "",
                    false
            );
            String jsonResponse = objectMapper.writeValueAsString(responseMap);

            mockServer.expect(requestTo(OCR_URL))
                    .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

            // when
            String result = ocrApiClient.extractIsbn(file);

            // then
            assertThat(result).isNull();
            mockServer.verify();
        }
    }
}
