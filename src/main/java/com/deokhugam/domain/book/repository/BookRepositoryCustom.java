package com.deokhugam.domain.book.repository;

import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Optional;
import java.util.UUID;

public interface BookRepositoryCustom {
    Optional<BookDto> findBookDetailById(UUID bookId);
    Slice<BookDto> findBooks(BookSearchCondition condition, Pageable pageable);
}
