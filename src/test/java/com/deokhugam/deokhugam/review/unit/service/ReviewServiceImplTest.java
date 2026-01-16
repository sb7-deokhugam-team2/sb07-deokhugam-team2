package com.deokhugam.deokhugam.review.unit.service;

import com. deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com. deokhugam.domain. review.entity.Review;
import com.deokhugam.domain.review.exception.ReviewAccessDeniedException;
import com.deokhugam.domain.review.exception.ReviewAlreadyExistsException;
import com.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.deokhugam.domain.review.mapper.ReviewMapper;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com. deokhugam.domain. review.service.ReviewServiceImpl;
import com.deokhugam.domain.user. entity.User;
import com. deokhugam.domain. user.repository.UserRepository;
import com.deokhugam.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org. junit.jupiter.api.DisplayName;
import org.junit. jupiter.api.Test;
import org.junit.jupiter.api. extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org. assertj.core.api.Assertions.*;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito. ArgumentMatchers. any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReviewService Test")
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private UUID testUserId;
    private UUID testBookId;
    private UUID testReviewId;
    private User testUser;
    private Book testBook;
    private ReviewCreateRequest validRequest;
    private ReviewUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {

        testUserId = UUID.randomUUID();
        testBookId = UUID.randomUUID();
        testReviewId = UUID.randomUUID();

        // Mock User 초기화
        testUser = mock(User.class);
        // Mock User ID 반환
        when(testUser.getId()).thenReturn(testUserId);

        // Mock Book 초기화
        testBook = mock(Book.class);
        // Mock Book ID 반환
        when(testBook.getId()).thenReturn(testBookId);

        // 테스트용 Request 생성
        validRequest = new ReviewCreateRequest(
                testBookId,
                testUserId,
                4.5,
                "정말 좋은 책입니다."
        );
        updateRequest = new ReviewUpdateRequest(
                3.5,
                "정말 나쁜 책입니다."
        );

    }

    @Test
    @DisplayName("성공: 리뷰 생성")
    void createReview_Success() {
        // given
        // Mock User 반환
        when(userRepository.findById(testUserId))
                .thenReturn(Optional.of(testUser));
        // Mock Book 반환
        when(bookRepository.findById(testBookId))
                .thenReturn(Optional.of(testBook));
        // 리뷰가 존재하지 않음
        when(reviewRepository.existsReviewByUserIdAndBookId(testUserId, testBookId))
                .thenReturn(false);

        // Mock Review 초기화 및 값 설정
        Review mockReview = mock(Review.class);
        when(mockReview.getUser()).thenReturn(testUser);
        when(mockReview.getBook()).thenReturn(testBook);
        when(mockReview.getId()).thenReturn(testReviewId);
        when(mockReview.getRating()).thenReturn(4.5);
        when(mockReview.getContent()).thenReturn("정말 좋은 책입니다.");
        when(mockReview.getLikedCount()).thenReturn(10L);
        when(mockReview.getCreatedAt()).thenReturn(Instant.now());
        when(mockReview.getUpdatedAt()).thenReturn(Instant.now());

        // ReviewRepository.save 결과 반환값 설정
        when(reviewRepository.save(any(Review.class))).thenReturn(mockReview);

        // Mapper Mock 설정
        when(reviewMapper.toReviewDto(mockReview, 0L, false)).thenReturn(new ReviewDto(
                testReviewId,
                testUserId,
                testBookId,
                "테스트 책 제목",
                "http://example.com/test-thumbnail",
                4.5,
                "테스트유저",
                "정말 좋은 책입니다.",
                10L,
                0L,
                false,
                Instant.now(),
                Instant.now()
        ));

        // when
        ReviewDto result = reviewService.createReview(validRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull().isEqualTo(testReviewId);
        assertThat(result.userId()).isEqualTo(testUserId);
        assertThat(result.bookId()).isEqualTo(testBookId);
        assertThat(result.rating()).isEqualTo(4.5);
        assertThat(result.content()).isEqualTo("정말 좋은 책입니다.");
        assertThat(result.likedCount()).isEqualTo(10L);

        verify(reviewMapper).toReviewDto(mockReview,0L, false);
        verify(reviewRepository).save(any(Review.class));

    }

    @Test
    @DisplayName("실패: 이미 활성 리뷰가 존재함")
    void createReview_Fail_ReviewAlreadyExists() {
        // given
        // 이미 리뷰 존재
        when(reviewRepository.existsReviewByUserIdAndBookId(testUserId, testBookId))
                .thenReturn(true);
        // Book Mock 반환
        when(bookRepository.findById(testBookId))
                .thenReturn(Optional.of(testBook));
        // User Mock 반환
        when(userRepository.findById(testUserId))
                .thenReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(validRequest))
                .isInstanceOf(ReviewAlreadyExistsException.class)
                .hasMessage(ErrorCode. REVIEW_ALREADY_EXISTS. getMessage());

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("성공: 본인이 작성한 리뷰 수정")
    void updateReview_Success() {
        // Given
        Review testReview = Review.create(4.5, "정말 좋은 책입니다.", testBook, testUser);
        when(reviewRepository.findById(testReviewId)).thenReturn(Optional.of(testReview)); // Review 조회 Mock
        when(reviewMapper.toReviewDto(any(Review.class), eq(0L), eq(false)))
                .thenReturn(new ReviewDto(
                        testReviewId,
                        testUserId,
                        testBookId,
                        "테스트 책 제목",
                        "http://example.com/test-thumbnail",
                        3.5,
                        "testUser",
                        "정말 나쁜 책입니다.",
                        20L,
                        0L,
                        false,
                        Instant.now(),
                        Instant.now()
                ));

        // When
        ReviewDto result = reviewService.updateReview(updateRequest, testUserId, testReviewId);

        // Then
        assertNotNull(result);
        assertEquals(3.5, result.rating(), "평점이 올바르게 수정되어야 함");
        assertEquals("정말 나쁜 책입니다.", result.content(), "내용이 올바르게 수정되어야 함");

        verify(reviewRepository).findById(testReviewId);
        verify(reviewMapper).toReviewDto(testReview, 0L, false);
    }

    @Test
    @DisplayName("실패: 다른 사용자가 리뷰 수정 시도")
    void updateReview_OtherUser_Fails() {

        // Given
        UUID anotherUserId = UUID.randomUUID();
        when(testUser.getId()).thenReturn(UUID.randomUUID());
        Review testReview = Review.create(4.5, "정말 좋은 책입니다.", testBook, testUser);

        when(reviewRepository.findById(testReviewId)).thenReturn(Optional.of(testReview)); // Review 조회 Mock

        // When & Then
        assertThrows(ReviewAccessDeniedException.class, () -> {
            reviewService.updateReview(updateRequest, testUserId, testReviewId);
        });

        verify(reviewRepository, times(1)).findById(testReviewId);
        verifyNoMoreInteractions(reviewRepository); // 추가 호출 없음
    }

    @Test
    @DisplayName("실패: 논리 삭제된 리뷰는 수정 불가")
    void updateReview_DeletedReview_Fails() {
        // Given
        Review testReview = Review.create(4.5, "정말 좋은 책입니다.", testBook, testUser);
        // 리뷰를 논리 삭제 상태로 전환
        testReview.delete();

        when(reviewRepository.findById(testReviewId))
                .thenReturn(Optional.of(testReview));

        // When & Then
        assertThrows(ReviewNotFoundException.class, () -> {
            reviewService.updateReview(updateRequest, testUserId, testReviewId);
        });

        verify(reviewRepository).findById(testReviewId);
        verifyNoMoreInteractions(reviewRepository);
    }

}
