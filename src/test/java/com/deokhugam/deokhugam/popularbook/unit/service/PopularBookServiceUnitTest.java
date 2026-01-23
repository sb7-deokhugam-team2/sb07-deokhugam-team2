package com.deokhugam.deokhugam.popularbook.unit.service;


import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.enums.SortDirection;
import com.deokhugam.domain.popularbook.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.popularbook.dto.response.CursorPageResponsePopularBookDto;
import com.deokhugam.domain.popularbook.dto.response.CursorResult;
import com.deokhugam.domain.popularbook.dto.response.PopularBookAggregationDto;
import com.deokhugam.domain.popularbook.dto.response.PopularBookDto;
import com.deokhugam.domain.popularbook.entity.PopularBook;
import com.deokhugam.domain.popularbook.mapper.PopularBookUrlMapper;
import com.deokhugam.domain.popularbook.policy.PopularBookQueryWindowCalculator;
import com.deokhugam.domain.popularbook.repository.PopularBookRepository;
import com.deokhugam.domain.popularbook.service.PopularBookServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class PopularBookServiceUnitTest {

    @Mock
    PopularBookRepository popularBookRepository;

    @Mock
    EntityManager em;

    // 이 메서드에서는 아래 2개가 쓰이긴 하지만, snapshotPopularBooks에서는 사용 안 하니까 Mock만 둬도 됨
    @Mock
    PopularBookQueryWindowCalculator popularBookQueryWindowCalculator;

    @Mock
    PopularBookUrlMapper popularBookUrlMapper;

    @InjectMocks
    PopularBookServiceImpl popularBookService;

    @Captor
    ArgumentCaptor<List<PopularBook>> popularBooksCaptor;

    @Captor
    ArgumentCaptor<Pageable> pageableCaptor;
    @Captor
    ArgumentCaptor<Instant> windowStartCaptor;

    @Nested
    @DisplayName("인기 도서 스냅샷 단위테스트")
    class SnapshotPopularBooks {
        @Test
        @DisplayName("[Behavior]인기 도서 스냅샷 저장 - 집계 결과를 rank 1..N으로 매핑해 PopularBook을 saveAll 위임")
        void snapshotPopularBooks_mapsAggregatesAndSaves() {
            // given
            UUID bookId1 = UUID.randomUUID();
            UUID bookId2 = UUID.randomUUID();

            List<PopularBookAggregationDto> aggregates = List.of(
                    new PopularBookAggregationDto(bookId1, 10L, 4.5, 99.0),
                    new PopularBookAggregationDto(bookId2, 9L, 4.8, 98.0)
            );

            given(popularBookRepository.findTopPopularBookAggregates(eq(PeriodType.DAILY), eq(10)))
                    .willReturn(aggregates);

            // getReference는 실제 엔티티가 없어도 프록시 반환 가능하니까 mock으로 대체
            Book bookRef1 = mock(Book.class);
            Book bookRef2 = mock(Book.class);
            given(em.getReference(Book.class, bookId1)).willReturn(bookRef1);
            given(em.getReference(Book.class, bookId2)).willReturn(bookRef2);

            // when
            popularBookService.snapshotPopularBooks(PeriodType.DAILY);

            // then
            then(popularBookRepository).should().saveAll(popularBooksCaptor.capture());

            List<PopularBook> saved = popularBooksCaptor.getValue();
            assertThat(saved).hasSize(2);

            // rank는 1부터 증가
            assertThat(saved).extracting(PopularBook::getRank)
                    .containsExactly(1L, 2L);

            // periodType 매핑
            assertThat(saved).allSatisfy(popularBook -> assertThat(popularBook.getPeriodType()).isEqualTo(PeriodType.DAILY));

            // score/rating/reviewCount 매핑 (엔티티 getter 이름은 너네 코드에 맞춰서 바꿔)
            assertThat(saved.get(0).getScore()).isEqualTo(99.0);
            assertThat(saved.get(0).getRating()).isEqualTo(4.5);
            assertThat(saved.get(0).getReviewCount()).isEqualTo(10L);

            assertThat(saved.get(1).getScore()).isEqualTo(98.0);
            assertThat(saved.get(1).getRating()).isEqualTo(4.8);
            assertThat(saved.get(1).getReviewCount()).isEqualTo(9L);

            // calculatedAt은 "같은 값"으로 들어가야 함(루프 밖에서 Instant.now() 한번 호출하니까)
            Instant calculatedAt0 = saved.get(0).getCalculatedDate();
            Instant calculatedAt1 = saved.get(1).getCalculatedDate();
            assertThat(calculatedAt0).isEqualTo(calculatedAt1);
        }

        @Test
        @DisplayName("[Behavior]인기 도서 스냅샷 저장 - 집계 결과가 비어있으면 빈 리스트로 saveAll 위임")
        void snapshotPopularBooks_savesEmptyListWhenNoAggregates() {
            // given
            given(popularBookRepository.findTopPopularBookAggregates(eq(PeriodType.DAILY), eq(10)))
                    .willReturn(List.of());

            // when
            popularBookService.snapshotPopularBooks(PeriodType.DAILY);

            // then
            then(popularBookRepository).should().saveAll(popularBooksCaptor.capture());
            assertThat(popularBooksCaptor.getValue()).isEmpty();
        }

    }

    @Nested
    @DisplayName("인기 도서 목록조회 단위테스트")
    class SearchPopularBooks {
        @Test
        @DisplayName("인기 도서 조회 - repository 결과에 hasNext=true면 nextCursor/nextAfter가 마지막 요소 기준으로 세팅된다")
        void searchPopularBooks_setsNextCursorAndAfter_whenHasNextTrue() {
            // given
            Instant now = Instant.parse("2026-01-22T00:00:00Z");
            Instant windowStart = Instant.parse("2026-01-21T00:00:00Z");

            given(popularBookQueryWindowCalculator.windowStart(any(Instant.class)))
                    .willReturn(windowStart);

            PopularBookSearchCondition condition = new PopularBookSearchCondition(
                    PeriodType.DAILY,
                    SortDirection.ASC,
                    null,
                    null,
                    2
            );

            PopularBookDto dto1 = new PopularBookDto(
                    UUID.randomUUID(), UUID.randomUUID(), "t1", "a1", "thumb1",
                    PeriodType.DAILY, 1L, 99.0, 10L, 5.0, Instant.parse("2026-01-21T10:00:00Z")
            );
            PopularBookDto dto2 = new PopularBookDto(
                    UUID.randomUUID(), UUID.randomUUID(), "t2", "a2", "thumb2",
                    PeriodType.DAILY, 2L, 98.0, 9L, 4.8, Instant.parse("2026-01-21T11:00:00Z")
            );

            CursorResult<PopularBookDto> cursorResult = new CursorResult<>(
                    List.of(dto1, dto2),
                    true,
                    100L
            );

            given(popularBookRepository.findTopPopularBooks(eq(condition), eq(windowStart), any(Pageable.class)))
                    .willReturn(cursorResult);

            // mapper는 content 그대로 반환(이번 테스트 목적은 nextCursor/nextAfter)
            given(popularBookUrlMapper.withFullThumbnailUrl(anyList()))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            CursorPageResponsePopularBookDto cursorPageResponsePopularBookDto = popularBookService.searchPopularBooks(condition);

            // then
            assertThat(cursorPageResponsePopularBookDto.content()).hasSize(2);
            assertThat(cursorPageResponsePopularBookDto.hasNext()).isTrue();
            assertThat(cursorPageResponsePopularBookDto.nextCursor()).isEqualTo("2"); // 마지막 요소 rank=2
            assertThat(cursorPageResponsePopularBookDto.nextAfter()).isEqualTo(Instant.parse("2026-01-21T11:00:00Z"));
            assertThat(cursorPageResponsePopularBookDto.totalElements()).isEqualTo(100L);
            assertThat(cursorPageResponsePopularBookDto.size()).isEqualTo(2); // content.size()

            // pageable=PageRequest.of(0, limit) 로 호출했는지도 확인
            then(popularBookRepository).should().findTopPopularBooks(eq(condition), eq(windowStart), pageableCaptor.capture());
            Pageable used = pageableCaptor.getValue();
            assertThat(used.getPageNumber()).isEqualTo(0);
            assertThat(used.getPageSize()).isEqualTo(2);
        }

        @Test
        @DisplayName("인기 도서 조회 - hasNext=false 또는 content 비어있으면 nextCursor/nextAfter는 null이다")
        void searchPopularBooks_nextCursorAfterNull_whenNoNextOrEmpty() {
            // given
            Instant windowStart = Instant.parse("2026-01-21T00:00:00Z");
            given(popularBookQueryWindowCalculator.windowStart(any(Instant.class)))
                    .willReturn(windowStart);

            PopularBookSearchCondition condition = new PopularBookSearchCondition(
                    PeriodType.DAILY,
                    SortDirection.ASC,
                    null,
                    null,
                    50
            );

            CursorResult<PopularBookDto> cursorResult = new CursorResult<>(
                    List.of(),   // empty
                    false,
                    0L
            );

            given(popularBookRepository.findTopPopularBooks(eq(condition), eq(windowStart), any(Pageable.class)))
                    .willReturn(cursorResult);

            given(popularBookUrlMapper.withFullThumbnailUrl(anyList()))
                    .willReturn(List.of());

            // when
            CursorPageResponsePopularBookDto cursorPageResponsePopularBookDto = popularBookService.searchPopularBooks(condition);

            // then
            assertThat(cursorPageResponsePopularBookDto.content()).isEmpty();
            assertThat(cursorPageResponsePopularBookDto.hasNext()).isFalse();
            assertThat(cursorPageResponsePopularBookDto.nextCursor()).isNull();
            assertThat(cursorPageResponsePopularBookDto.nextAfter()).isNull();
            assertThat(cursorPageResponsePopularBookDto.totalElements()).isEqualTo(0L);
            assertThat(cursorPageResponsePopularBookDto.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("인기 도서 조회 - UrlMapper 변환 결과가 응답 content에 반영된다")
        void searchPopularBooks_appliesUrlMapperResult() {
            // given
            Instant windowStart = Instant.parse("2026-01-21T00:00:00Z");
            given(popularBookQueryWindowCalculator.windowStart(any(Instant.class)))
                    .willReturn(windowStart);

            PopularBookSearchCondition condition = new PopularBookSearchCondition(
                    PeriodType.DAILY,
                    SortDirection.ASC,
                    null,
                    null,
                    1
            );

            PopularBookDto bookDto = new PopularBookDto(
                    UUID.randomUUID(), UUID.randomUUID(), "t1", "a1", "thumb-raw",
                    PeriodType.DAILY, 1L, 99.0, 10L, 5.0, Instant.parse("2026-01-21T11:00:00Z")
            );

            CursorResult<PopularBookDto> cursorResult = new CursorResult<>(
                    List.of(bookDto),
                    false,
                    1L
            );

            given(popularBookRepository.findTopPopularBooks(eq(condition), eq(windowStart), any(Pageable.class)))
                    .willReturn(cursorResult);

            PopularBookDto mapped = new PopularBookDto(
                    bookDto.id(), bookDto.bookId(), bookDto.title(), bookDto.author(),
                    "https://cdn.example.com/" + bookDto.thumbnailUrl(),
                    bookDto.period(), bookDto.rank(), bookDto.score(), bookDto.reviewCount(), bookDto.rating(), bookDto.createdAt()
            );

            given(popularBookUrlMapper.withFullThumbnailUrl(eq(List.of(bookDto))))
                    .willReturn(List.of(mapped));

            // when
            CursorPageResponsePopularBookDto res = popularBookService.searchPopularBooks(condition);

            // then
            assertThat(res.content()).hasSize(1);
            assertThat(res.content().get(0).thumbnailUrl()).isEqualTo("https://cdn.example.com/thumb-raw");
            assertThat(res.hasNext()).isFalse();
            assertThat(res.nextCursor()).isNull();
            assertThat(res.nextAfter()).isNull();

            // mapper가 "repoResult.content()"로 호출됐는지도 확인
            then(popularBookUrlMapper).should().withFullThumbnailUrl(eq(List.of(bookDto)));
        }
    }
}
