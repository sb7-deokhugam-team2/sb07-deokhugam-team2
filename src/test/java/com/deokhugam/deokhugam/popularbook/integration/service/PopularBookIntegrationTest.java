package com.deokhugam.deokhugam.popularbook.integration.service;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.enums.SortDirection;
import com.deokhugam.domain.popularbook.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.popularbook.dto.response.CursorPageResponsePopularBookDto;
import com.deokhugam.domain.popularbook.dto.response.PopularBookDto;
import com.deokhugam.domain.popularbook.entity.PopularBook;
import com.deokhugam.domain.popularbook.mapper.PopularBookUrlMapper;
import com.deokhugam.domain.popularbook.policy.PopularBookQueryWindowCalculator;
import com.deokhugam.domain.popularbook.service.PopularBookService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
public class PopularBookIntegrationTest {

    @Autowired
    PopularBookService popularBookService;
    @Autowired
    EntityManager em;

    @MockitoBean
    PopularBookQueryWindowCalculator popularBookQueryWindowCalculator; // 실제로 쓰면 실행 시점마다 결과가 달라지기에 mock

    @MockitoBean
    PopularBookUrlMapper popularBookUrlMapper; // 실제로 쓰면 s3/cdn , 환경설정 등 의존하게 되므로 mock, 테스트목적과 검증 하고싶은 책임이 아님

    @Nested
    @DisplayName("인기 도서 연산 서비스 - snapShotPopularBooks")
    class SnapShotPopularBooks {

    }

    @Nested
    @DisplayName("인기 도서 목록 조회 - searchPopularBooks")
    class SearchPopularBooks {

        @Test
        @DisplayName("인기 도서 조회 - windowStart 이후 스냅샷 중 최신(calculatedDate max) 스냅샷만 반환된다")
        void searchPopularBooks_picksLatestSnapshotWithinWindow() {
            // given
            Book book1 = persistBook("t1", "a1", "isbn-1", "thumb-1");
            Book book2 = persistBook("t2", "a2", "isbn-2", "thumb-2");

            Instant now = Instant.now();
            Instant oldSnap = now.minus(10, ChronoUnit.MINUTES);
            Instant latestSnap = now.minus(2, ChronoUnit.MINUTES);

            // old snapshot
            em.persist(PopularBook.create(PeriodType.DAILY, oldSnap, 1L, 10.0, 4.0, 2L, book1));
            em.persist(PopularBook.create(PeriodType.DAILY, oldSnap, 2L,  9.0, 3.5, 1L, book2));

            // latest snapshot
            em.persist(PopularBook.create(PeriodType.DAILY, latestSnap, 1L, 99.0, 5.0, 10L, book2));
            em.persist(PopularBook.create(PeriodType.DAILY, latestSnap, 2L, 98.0, 4.8,  9L, book1));

            em.flush();
            em.clear();

            // windowStart는 테스트에서 고정(서비스가 지금시간을 쓰더라도 결과가 흔들리지 않게)
            given(popularBookQueryWindowCalculator.windowStart(any(Instant.class)))
                    .willReturn(now.minus(30, ChronoUnit.MINUTES));

            // mapper는 일단 identity로 (이번 테스트는 "최신 스냅샷 선택"이 목적)
            given(popularBookUrlMapper.withFullThumbnailUrl(anyList()))
                    .willAnswer(invocation -> invocation.getArgument(0));

            PopularBookSearchCondition condition = new PopularBookSearchCondition(
                    PeriodType.DAILY,
                    SortDirection.ASC,
                    null,
                    null,
                    50
            );

            // when
            CursorPageResponsePopularBookDto cursorPageResponsePopularBookDto = popularBookService.searchPopularBooks(condition);

            // then
            assertThat(cursorPageResponsePopularBookDto.content()).hasSize(2);
            // 최신 스냅샷의 점수만 나와야 함
            assertThat(cursorPageResponsePopularBookDto.content()).extracting(PopularBookDto::score)
                    .containsExactly(99.0, 98.0);
        }

        @Test
        @DisplayName("인기 도서 조회 - 다음 페이지가 있으면 nextCursor/nextAfter가 채워지고, cursor로 다음 페이지 조회가 된다")
        void searchPopularBooks_setsNextCursorAndSupportsCursorPaging() {
            // given
            Book book1 = persistBook("t1", "a1", "isbn-1", "thumb-1");
            Book book2 = persistBook("t2", "a2", "isbn-2", "thumb-2");

            Instant now = Instant.now();
            Instant snap = now.minus(2, ChronoUnit.MINUTES);

            // 최신 스냅샷 2개 (rank 1,2)
            em.persist(PopularBook.create(PeriodType.DAILY, snap, 1L, 99.0, 5.0, 10L, book2));
            em.persist(PopularBook.create(PeriodType.DAILY, snap, 2L, 98.0, 4.8,  9L, book1));

            em.flush();
            em.clear();

            given(popularBookQueryWindowCalculator.windowStart(any(Instant.class)))
                    .willReturn(now.minus(30, ChronoUnit.MINUTES));
            given(popularBookUrlMapper.withFullThumbnailUrl(anyList()))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // 1) 첫 페이지(limit=1) → hasNext=true, nextCursor/nextAfter 세팅
            PopularBookSearchCondition firstCondition = new PopularBookSearchCondition(
                    PeriodType.DAILY,
                    SortDirection.ASC,
                    null,
                    null,
                    1
            );

            CursorPageResponsePopularBookDto firstPage = popularBookService.searchPopularBooks(firstCondition);

            assertThat(firstPage.content()).hasSize(1);
            assertThat(firstPage.hasNext()).isTrue();
            assertThat(firstPage.nextCursor()).isEqualTo("1");   // 마지막 요소 rank가 1이라면
            assertThat(firstPage.nextAfter()).isNotNull();

            // 2) 다음 페이지(cursor=1, after=first.nextAfter) → rank=2 한 건만
            PopularBookSearchCondition secondCondition = new PopularBookSearchCondition(
                    PeriodType.DAILY,
                    SortDirection.ASC,
                    firstPage.nextCursor(),
                    firstPage.nextAfter(),
                    1
            );

            CursorPageResponsePopularBookDto secondPage = popularBookService.searchPopularBooks(secondCondition);
            System.out.println("secondPage = " + secondPage.content());
            assertThat(secondPage.content()).hasSize(1); // TODO: 2개나오는것, 즉 제대로 cursor안되는거보기
            assertThat(secondPage.content().get(0).rank()).isEqualTo(2L);
            assertThat(secondPage.hasNext()).isFalse();
            assertThat(secondPage.nextCursor()).isNull();
            assertThat(secondPage.nextAfter()).isNull();
        }

        @Test
        @DisplayName("인기 도서 조회 - UrlMapper가 만든 썸네일 URL 변환 결과가 응답 content에 반영된다")
        void searchPopularBooks_appliesUrlMapper() {
            // given
            Book book1 = persistBook("t1", "a1", "isbn-1", "thumb-raw");
            Instant now = Instant.now();
            Instant snap = now.minus(2, ChronoUnit.MINUTES);

            em.persist(PopularBook.create(PeriodType.DAILY, snap, 1L, 99.0, 5.0, 10L, book1));

            em.flush();
            em.clear();

            given(popularBookQueryWindowCalculator.windowStart(any(Instant.class)))
                    .willReturn(now.minus(30, ChronoUnit.MINUTES));

            // mapper가 "full url"로 바꿔서 돌려준다고 가정하고, 서비스가 그걸 그대로 응답에 쓰는지 검증
            given(popularBookUrlMapper.withFullThumbnailUrl(anyList()))
                    .willAnswer(invocation -> {
                        List<PopularBookDto> original = invocation.getArgument(0);
                        PopularBookDto popularBookDto = original.get(0);

                        PopularBookDto mapped = new PopularBookDto(
                                popularBookDto.id(),
                                popularBookDto.bookId(),
                                popularBookDto.title(),
                                popularBookDto.author(),
                                "https://cdn.example.com/" + popularBookDto.thumbnailUrl(),
                                popularBookDto.period(),
                                popularBookDto.rank(),
                                popularBookDto.score(),
                                popularBookDto.reviewCount(),
                                popularBookDto.rating(),
                                popularBookDto.createdAt()
                        );
                        return List.of(mapped);
                    });

            PopularBookSearchCondition condition = new PopularBookSearchCondition(
                    PeriodType.DAILY,
                    SortDirection.ASC,
                    null,
                    null,
                    50
            );

            // when
            CursorPageResponsePopularBookDto res = popularBookService.searchPopularBooks(condition);

            // then
            assertThat(res.content()).hasSize(1);
            assertThat(res.content().get(0).thumbnailUrl()).startsWith("https://cdn.example.com/");
        }

        private Book persistBook(String title, String author, String isbn, String thumb) {
            Book book = Book.create(title, author, isbn, LocalDate.now(), "publisher", thumb, "desc");
            em.persist(book);
            return book;
        }

    }

}
