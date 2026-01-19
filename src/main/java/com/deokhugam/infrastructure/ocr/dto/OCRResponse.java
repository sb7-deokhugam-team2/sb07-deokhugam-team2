package com.deokhugam.infrastructure.ocr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OCRResponse(
        @JsonProperty("ParsedResults") List<ParsedResult> parsedResults,
        @JsonProperty("ErrorMessage") String errorMessage, // API 에러 메시지 필드 추가
        @JsonProperty("IsErroredOnProcessing") boolean isErrored
) {
    public record ParsedResult(
            @JsonProperty("ParsedText") String parsedText,
            @JsonProperty("ErrorMessage") String errorMessage
    ){}
}
