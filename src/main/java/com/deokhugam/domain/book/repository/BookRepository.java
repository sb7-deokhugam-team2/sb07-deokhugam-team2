package com.deokhugam.domain.book.repository;

import com.deokhugam.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID>, BookRepositoryCustom {
    Book findBookById(UUID id);

    boolean existsByIsbn(String isbn);
}
