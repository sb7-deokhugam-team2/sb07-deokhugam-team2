package com.deokhugam.global.api;

import com.deokhugam.domain.book.dto.response.NaverBookDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class NaverBookApiClient implements ApiDataClient<NaverBookDto>{

    private final RestTemplate restTemplate;

    @Value("${api.naver.client.ID}")
    private String clientId;

    @Value("${api.naver.client.SECRET}")
    private String clientSecret;

    @Value("${api.naver.url.search}")
    private String naverUrl;

    @Override
    public NaverBookDto getData(String condition) {
        URI uri = UriComponentsBuilder.fromHttpUrl(naverUrl)
                .queryParam("query", condition)
                .queryParam("display", 1) // 1개만 검색
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<NaverBookDto> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                request,
                NaverBookDto.class
        );
        return null;
    }
}
