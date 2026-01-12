package com.deokhugam.deokhugam.book.integration.slice.jpa;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.global.config.JpaAuditingConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("BookRepository JPA Slice Test")
public class BookRepositorySliceTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    EntityManager em;

    @Nested
    @DisplayName("도서등록 - BookSave")
    class saveBook{

        @Test
        @DisplayName("[Success] 도서 등록 - 도서등록후 같은 엔티티로 조회")
        void saveBook_should_success_and_same(){
            //given
            Book book = Book.create("title", "author", "1234567890123", LocalDate.now(), "publisher", null, "description");

            // when
            Book savedBook = bookRepository.save(book);
            em.flush();
            em.clear();

            Book found = bookRepository.findById(savedBook.getId()).orElseThrow();

            // then
            assertEquals(book.getId(), found.getId());
            assertEquals(book.getTitle(), found.getTitle());
            assertEquals(book.getAuthor(), found.getAuthor());
            assertEquals(book.getIsbn(), found.getIsbn());
            assertEquals(book.getPublishedDate(), found.getPublishedDate());
            assertEquals(book.getPublisher(), found.getPublisher());
            assertEquals(book.getDescription(), found.getDescription());
            assertNotNull(found.getCreatedAt());
        }
    }

}
