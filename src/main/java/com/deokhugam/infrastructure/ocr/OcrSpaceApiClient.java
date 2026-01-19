package com.deokhugam.infrastructure.ocr;

import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.infrastructure.ocr.dto.OcrResponse;
import com.deokhugam.infrastructure.ocr.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcrSpaceApiClient implements IsbnExtractor{

    private final RestTemplate restTemplate;

    @Value("${api.ocr.key}")
    private String ocrApiKey;

    private static final String OCR_URL = "https://api.ocr.space/parse/image";

    private static final Pattern ISBN_PATTERN = Pattern.compile("(978|979)\\d{10}");

    @Override
    public String extractIsbn(MultipartFile file) {
        log.info("OCR 이미지 분석을 시작합니다. 파일명: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new OcrFileEmptyException(ErrorCode.OCR_EMPTY_FILE_EXCEPTION);
        }

        try {
            HttpEntity<MultiValueMap<String, Object>> requestEntity = createRequest(file);

            ResponseEntity<OcrResponse> response = restTemplate.exchange(
                    OCR_URL,
                    HttpMethod.POST,
                    requestEntity,
                    OcrResponse.class
            );

            return parseResponse(response);

        } catch (IOException e) {
            log.error("OCR 파일 처리에 실패했습니다. 파일명: {}", file.getOriginalFilename(), e);
            throw new OrcFileProcessingException(ErrorCode.OCR_FILE_PROCESSING_ERROR);
        } catch (RestClientException e) {
            log.error("OCR API 연동 중 오류가 발생했습니다. 에러: {}", e.getMessage());
            throw new OcrApiConnectionException(ErrorCode.OCR_API_CONNECTION_EXCEPTION);
        }
    }

    private HttpEntity<MultiValueMap<String, Object>> createRequest(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("apikey", ocrApiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("language", "kor"); //
        body.add("isOverlayRequired", "false");
        body.add("detectOrientation", "true");
        body.add("scale", "true");

        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        body.add("file", fileResource);

        return new HttpEntity<>(body, headers);
    }

    private String parseResponse(ResponseEntity<OcrResponse> response) {
        OcrResponse body = response.getBody();

        if (body == null || body.parsedResults() == null || body.parsedResults().isEmpty()) {
            log.warn("OCR API 응답이 비어있거나 결과가 없습니다.");
            throw new OcrIsbnExtractFailedException(ErrorCode.OCR_ISBN_EXTRACT_FAILED);
        }
        if (StringUtils.hasText(body.errorMessage())) {
            log.error("OCR API 내부 오류 발생: {}", body.errorMessage());
            throw new OcrApiInternalException(ErrorCode.OCR_API_INTERNAL_ERROR);
        }

        String extractedText = String.valueOf(body.parsedResults().get(0));
        log.debug("OCR 원본 추출 텍스트: {}", extractedText);

        String isbn = findIsbn(extractedText);

        if (isbn == null) {
            log.warn("이미지에서 ISBN 패턴을 찾을 수 없습니다.");
        }

        log.info("ISBN 추출 성공: {}", isbn);
        return isbn;
    }

    private String findIsbn(String text) {
        if (!StringUtils.hasText(text)) return null;

        String cleanText = text.replaceAll("[^0-9]", "");

        Matcher matcher = ISBN_PATTERN.matcher(cleanText);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
