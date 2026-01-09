package com.deokhugam.domain.book.entity;

import com.deokhugam.domain.base.BaseDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "books")
@AllArgsConstructor
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
}
