package com.deokhugam.infrastructure.search.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record NaverApiResponse(List<NaverItem> items) {
    public record NaverItem(
            String title,
            String author,
            String description,
            String publisher,
            String isbn,
            String image,       // URL 문자열로 받음
            @JsonProperty("pubdate") String pubdate // JSON 키는 소문자 pubdate
    ) {}
}
