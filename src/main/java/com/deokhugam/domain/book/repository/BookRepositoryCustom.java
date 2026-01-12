package com.deokhugam.domain.book.repository;

import com.deokhugam.domain.book.dto.response.BookDto;

import java.util.Optional;
import java.util.UUID;

public interface BookRepositoryCustom {
    Optional<BookDto> findBookDetailById(UUID bookId);
}
