package com.deokhugam.deokhugam.review.unit.service;

import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.comment.repository.CommentRepository;
import com.deokhugam.domain.review.dto.request.CursorPageRequest;
import com.deokhugam.domain.review.dto.request.ReviewSearchCondition;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.dto.response.ReviewPageResponseDto;
import com.deokhugam.domain.review.enums.ReviewOrderBy;
import com.deokhugam.domain.review.enums.SortDirection;
import com.deokhugam.domain.review.exception.ReviewInvalidException;
import com.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.deokhugam.domain.review.mapper.ReviewMapper;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.review.service.ReviewServiceImpl;
import com.deokhugam.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceUnitTest {
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewMapper reviewMapper;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    @DisplayName("목록 조회 성공")
    void searchReview_success() {
        // given
        UUID requestId = UUID.randomUUID();

        ReviewSearchCondition condition = null;

        CursorPageRequest cursorPageRequest = new CursorPageRequest(
                ReviewOrderBy.CREATED_AT,
                SortDirection.DESC,
                null,
                null,
                20
        );

        ReviewPageResponseDto expected = new ReviewPageResponseDto(
                List.of(),
                null,
                null,
                0,
                0L,
                false
        );

        when(reviewRepository.search(condition, cursorPageRequest, requestId)).thenReturn(expected);

        // when
        ReviewPageResponseDto result = reviewService.searchReviews(condition, cursorPageRequest, requestId);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(reviewRepository, times(1)).search(condition, cursorPageRequest, requestId);
        
    }

    @Test
    @DisplayName("목록 조회 실패 - Rating 정렬 시 after값이 없다면 실패")
    void searchReview_fail() {
        // given
        UUID requestId = UUID.randomUUID();
        ReviewSearchCondition condition = null;
        CursorPageRequest cursorPageRequest = new CursorPageRequest(
                ReviewOrderBy.RATING,
                SortDirection.DESC,
                "5.0",
                null,
                20
        );

        // when & then
        assertThrows(ReviewInvalidException.class,
                () -> reviewService.searchReviews(condition, cursorPageRequest, requestId));
        verify(reviewRepository, never()).search(any(), any(), any());
    }

    @Test
    @DisplayName("상세 조회 성공")
    void getReview_success() {
        // given
        UUID requestId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        Instant now = Instant.now();

        ReviewDto expected = new ReviewDto(
                reviewId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "bookTitle",
                "bookThumbnailUrl",
                5.0,
                "nickname",
                "content",
                10L,
                3L,
                true,
                now,
                now
        );

        when(reviewRepository.findDetail(reviewId, requestId)).thenReturn(Optional.of(expected));

        // when
        ReviewDto result = reviewService.getReview(requestId, reviewId);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(reviewRepository, times(1)).findDetail(reviewId, requestId);

    }

    @Test
    @DisplayName("상세 조회 실패")
    void getReview_fail() {
        // given
        UUID requestId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        when(reviewRepository.findDetail(reviewId, requestId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ReviewNotFoundException.class,
                () -> reviewService.getReview(requestId, reviewId));
        verify(reviewRepository, times(1)).findDetail(reviewId, requestId);

    }
}
