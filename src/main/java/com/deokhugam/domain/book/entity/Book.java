package com.deokhugam.domain.book.entity;

import com.deokhugam.domain.base.BaseDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "books")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseDeletableEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "isbn", nullable = false, unique = true)
    private String isbn;

    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    @Column(name = "publisher", nullable = false)
    private String publisher;

    @Column(name = "thumbnail_url", nullable = true)
    private String thumbnailUrl;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    private Book(String title, String author, String isbn, LocalDate publishedDate, String publisher, String thumbnailUrl, String description) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publishedDate = publishedDate;
        this.publisher = publisher;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
    }

    public static Book create(String title, String author, String isbn,
                              LocalDate publishedDate, String publisher,
                              String thumbnailUrl, String description){
        return new Book(title, author, isbn, publishedDate, publisher, thumbnailUrl, description);
    }

}
