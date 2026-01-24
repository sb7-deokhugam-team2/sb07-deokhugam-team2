package com.deokhugam.domain.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookUpdateRequest(
        @NotBlank
        String title,
        @NotBlank
        String author,
        @NotBlank
        String description,
        @NotNull
        String publisher,
        @NotBlank
        LocalDate publishedDate
) {
}
