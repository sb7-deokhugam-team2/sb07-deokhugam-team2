package com.deokhugam.domain.book.repository;

import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.popularbook.dto.response.CursorResult;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface BookRepositoryCustom {
    Optional<BookDto> findBookDetailById(UUID bookId);
    CursorResult<BookDto> findBooks(BookSearchCondition condition, Pageable pageable);
    long countTotal(BookSearchCondition condition);
}
