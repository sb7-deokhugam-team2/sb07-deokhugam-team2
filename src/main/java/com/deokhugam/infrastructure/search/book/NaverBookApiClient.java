package com.deokhugam.infrastructure.search.book;

import com.deokhugam.infrastructure.search.book.dto.BookGlobalApiDto;
import com.deokhugam.infrastructure.search.book.enums.ProviderType;
import com.deokhugam.infrastructure.search.book.dto.NaverApiResponse;
import com.deokhugam.infrastructure.search.book.exception.ApiConnectionException;
import com.deokhugam.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class NaverBookApiClient implements DataApiClient {

    private final RestTemplate restTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${api.naver.client.ID}")
    private String clientId;

    @Value("${api.naver.client.SECRET}")
    private String clientSecret;

    @Value("${api.naver.url.search}")
    private String naverUrl;

    @Override
    public BookGlobalApiDto getData(String isbn) {
        log.info("네이버 도서 검색을 시작합니다. 요청 ISBN: {}", isbn);

        NaverApiResponse.NaverItem item = fetchBookInfo(isbn);

        return mapToDto(item);
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.NAVER;
    }

    private NaverApiResponse.NaverItem fetchBookInfo(String isbn) {
        URI uri = UriComponentsBuilder.fromHttpUrl(naverUrl)
                .queryParam("query", isbn)
                .queryParam("display", 1)
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<NaverApiResponse> response = restTemplate.exchange(
                    uri, HttpMethod.GET, request, NaverApiResponse.class
            );

            NaverApiResponse body = response.getBody();
            if (body == null || body.items() == null || body.items().isEmpty()) {
                log.warn("네이버 API 검색 결과가 없습니다. ISBN: {}", isbn);
            }

            return body.items().get(0);

        } catch (RestClientException e) {
            log.error("네이버 API 연동에 실패했습니다. ISBN: {}, 에러: {}", isbn, e.getMessage());
            throw new ApiConnectionException(ErrorCode.NAVER_API_CONNECTION_ERROR);
        }
    }

    private BookGlobalApiDto mapToDto(NaverApiResponse.NaverItem item) {
        LocalDate pubDate = parsePubDate(item.pubdate());
        byte[] imageBytes = downloadImage(item.image());

        return new BookGlobalApiDto(
                item.title(),
                item.author(),
                item.description(),
                item.publisher(),
                pubDate,
                item.isbn(),
                imageBytes,
                getProviderType()
        );
    }

    private LocalDate parsePubDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("날짜 파싱에 실패했습니다. 입력값: {}. 날짜 정보를 건너뜁니다.", dateStr);
            return null;
        }
    }

    private byte[] downloadImage(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return null;
        }
        try {
            return restTemplate.getForObject(imageUrl, byte[].class);
        } catch (Exception e) {
            log.warn("썸네일 이미지 다운로드에 실패했습니다. URL: {}, 에러: {}", imageUrl, e.getMessage());
            return null;
        }
    }
}
