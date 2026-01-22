package com.deokhugam.domain.popularbook.service;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.popularbook.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.popularbook.dto.response.CursorPageResponsePopularBookDto;
import com.deokhugam.domain.popularbook.dto.response.CursorResult;
import com.deokhugam.domain.popularbook.dto.response.PopularBookAggregationDto;
import com.deokhugam.domain.popularbook.dto.response.PopularBookDto;
import com.deokhugam.domain.popularbook.entity.PopularBook;
import com.deokhugam.domain.popularbook.mapper.PopularBookUrlMapper;
import com.deokhugam.domain.popularbook.policy.PopularBookQueryWindowCalculator;
import com.deokhugam.domain.popularbook.repository.PopularBookRepository;
import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PopularBookServiceImpl implements PopularBookService {

    private final PopularBookRepository popularBookRepository;
    private final EntityManager em;
    private final PopularBookQueryWindowCalculator popularBookQueryWindowCalculator;
    private final PopularBookUrlMapper popularBookUrlMapper;

    @Override
    @Transactional
    public void snapshotPopularBooks(PeriodType periodType) {
        /// NOTE: 한 도서의 점수 = (해당 기간의 리뷰수 * 0.4) + (해당 기간의 평점 편균 * 0.6)
        /// NOTE: 방법 1. 결국 목록 조회와 같은 쿼리로 모든 도서 범위이면서 + 조건(한 도서 점수)로 조건을 만들고 그 결과의 limit으로 10개 해서 top만 select 호출하도록
        int POPULAR_BOOK_CALCULATE_LIMIT = 10;
        switch (periodType) {
            case DAILY, WEEKLY, MONTHLY, ALL_TIME:
                List<PopularBookAggregationDto> topPopularBookAggregates = popularBookRepository.findTopPopularBookAggregates(periodType, POPULAR_BOOK_CALCULATE_LIMIT);
                long rank = 1;
                Instant calculatedAt = Instant.now();
                List<PopularBook> popularBooks = new ArrayList<>(topPopularBookAggregates.size());
                for (PopularBookAggregationDto topPopularBookAggregate : topPopularBookAggregates) {
                    Book bookReference = em.getReference(Book.class, topPopularBookAggregate.bookId());
                    PopularBook popularBook = PopularBook.create(
                            periodType,
                            calculatedAt,
                            rank++,
                            topPopularBookAggregate.score(),
                            topPopularBookAggregate.avgRating(),
                            topPopularBookAggregate.reviewCount(),
                            bookReference
                    );
                    popularBooks.add(popularBook);
                }
                popularBookRepository.saveAll(popularBooks); // TODO: N번 호출되는거 확인후, batch 설정비교
                break;
            default:
                throw new DeokhugamException(ErrorCode.POPULAR_BOOK_INVALID_PERIOD_TYPE, Map.of("periodType", String.valueOf(periodType)));
        }
    }

    @Override
    public CursorPageResponsePopularBookDto searchPopularBooks(PopularBookSearchCondition popularBookSearchCondition) {

        Pageable pageable = PageRequest.of(0, popularBookSearchCondition.limit());
        Instant windowStart = popularBookQueryWindowCalculator.windowStart(Instant.now());
        CursorResult<PopularBookDto> pagePopularBook = popularBookRepository.findTopPopularBooks(popularBookSearchCondition, windowStart, pageable);
        List<PopularBookDto> content = popularBookUrlMapper.withFullThumbnailUrl(pagePopularBook.content());
        PopularBookDto lastPopularBook = content.isEmpty() ? null : content.get(content.size() - 1);

        String nextCursor = null;
        Instant nextAfter = null;
        if (pagePopularBook.hasNext() && lastPopularBook != null) {
            nextCursor = lastPopularBook.rank().toString();
            nextAfter = lastPopularBook.createdAt();
        }

        return new CursorPageResponsePopularBookDto(
                content,
                nextCursor,
                nextAfter,
                content.size(),
                pagePopularBook.total(),
                pagePopularBook.hasNext()
        );
    }
}

