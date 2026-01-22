package com.deokhugam.infrastructure.search.book;

import com.deokhugam.domain.book.exception.BookISBNNotFoundException;
import com.deokhugam.infrastructure.search.book.dto.BookGlobalApiDto;
import com.deokhugam.infrastructure.search.book.enums.ProviderType;
import com.deokhugam.infrastructure.search.book.dto.NaverApiResponse;
import com.deokhugam.infrastructure.search.book.exception.ApiConnectionException;
import com.deokhugam.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
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

    private final RestClient restClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${api.naver.client.ID}")
    private String clientId;

    @Value("${api.naver.client.SECRET}")
    private String clientSecret;

    @Value("${api.naver.url.search}")
    private String naverUrl;

    @Override
    public BookGlobalApiDto getData(String isbn) {
        long startTime = System.currentTimeMillis();
        log.info("네이버 도서 검색을 시작합니다. 요청 ISBN: {}", isbn);

        NaverApiResponse.NaverItem item = fetchBookInfo(isbn);
        BookGlobalApiDto result = mapToDto(item);

        long duration = System.currentTimeMillis() - startTime;

        log.info("[NaverAPI] 도서 검색 성공. ISBN: {}, Title: '{}', HasImage: {}, Time: {}ms",
                isbn,
                result.title(),
                (result.thumbnailImage() != null && result.thumbnailImage().length > 0),
                duration
        );

        return result;
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.NAVER;
    }

    private NaverApiResponse.NaverItem fetchBookInfo(String isbn) {
        URI uri = UriComponentsBuilder.fromUriString(naverUrl)
                .queryParam("query", isbn)
                .queryParam("display", 1)
                .build()
                .toUri();

        try {
            NaverApiResponse body = restClient.get()
                    .uri(uri)
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(NaverApiResponse.class);

            if (body == null || body.items() == null || body.items().isEmpty()) {
                log.warn("네이버 API 검색 결과가 없습니다. ISBN: {}", isbn);
                throw new BookISBNNotFoundException(ErrorCode.BOOK_NOT_FOUND_IN_API);
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
            return restClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .body(byte[].class);
        } catch (Exception e) {
            log.warn("썸네일 이미지 다운로드에 실패했습니다. URL: {}, 에러: {}", imageUrl, e.getMessage());
            return null;
        }
    }
}
