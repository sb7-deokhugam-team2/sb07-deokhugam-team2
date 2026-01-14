package com.deokhugam.domain.book.repository;

import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface BookRepositoryCustom {
    Optional<BookDto> findBookDetailById(UUID bookId);
    Page<BookDto> findBooks(BookSearchCondition condition, Pageable pageable);
}
