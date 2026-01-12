package com.deokhugam.domain.book.repository;

import com.deokhugam.domain.book.dto.response.BookDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

import static com.deokhugam.domain.book.entity.QBook.book;
import static com.deokhugam.domain.review.entity.QReview.review;

@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<BookDto> findBookDetailById(UUID bookId) {

        BookDto dto = queryFactory
                .select(Projections.constructor(
                        BookDto.class,
                        book.id,
                        book.title,
                        book.author,
                        book.description,
                        book.publisher,
                        book.publishedDate,
                        book.isbn,
                        book.thumbnailUrl,
                        review.id.countDistinct().coalesce(0L),
                        review.rating.avg().coalesce(0.0),
                        book.createdAt,
                        book.updatedAt
                ))
                .from(book)
                .leftJoin(review).on(review.book.id.eq(book.id))
                .where(book.id.eq(bookId))
                .groupBy(
                        book.id,
                        book.title,
                        book.author,
                        book.description,
                        book.publisher,
                        book.publishedDate,
                        book.isbn,
                        book.thumbnailUrl,
                        book.createdAt,
                        book.updatedAt
                )
                .fetchOne();

        return Optional.ofNullable(dto);
    }
}
