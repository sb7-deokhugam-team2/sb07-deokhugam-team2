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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org. assertj.core.api.Assertions.*;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
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
        when(testUser.getId()).thenReturn(testUserId);
        when(testUser.getNickname()).thenReturn("Mock User Name");

        // Mock Book 초기화
        testBook = mock(Book.class);
        when(testBook.getId()).thenReturn(testBookId);
        when(testBook.getTitle()).thenReturn("Mock Book Title");

        // 테스트용 Request 생성
        validRequest = new ReviewCreateRequest(
                testBookId,
                testUserId,
                4.0,
                "정말 좋은 책입니다."
        );
        updateRequest = new ReviewUpdateRequest(
                3.0,
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
        when(mockReview.getRating()).thenReturn(4.0);
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
                4.0,
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
        assertThat(result.rating()).isEqualTo(4.0);
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
        // given
        Review testReview = Review.create(4.0, "정말 좋은 책입니다.", testBook, testUser);
        when(reviewRepository.findById(testReviewId)).thenReturn(Optional.of(testReview)); // Review 조회 Mock
        when(reviewRepository.findDetail(testReviewId, testUserId))
                .thenReturn(Optional.of(new ReviewDto(
                        testReviewId,
                        testUserId,
                        testBookId,
                        "테스트 책 제목",
                        "http://example.com/test-thumbnail",
                        3.0,
                        "테스트유저",
                        "정말 나쁜 책입니다.",
                        10L,
                        2L,
                        true,
                        testReview.getCreatedAt(),
                        Instant.now()
                )));

        // when
        ReviewDto result = reviewService.updateReview(updateRequest, testUserId, testReviewId);

        // then
        assertNotNull(result);
        assertEquals(3.0, result.rating(), "평점이 올바르게 수정되어야 함");
        assertEquals("정말 나쁜 책입니다.", result.content(), "내용이 올바르게 수정되어야 함");

        verify(reviewRepository).findById(testReviewId);
        verify(reviewRepository).findDetail(testReviewId, testUserId);
    }

    @Test
    @DisplayName("실패: 다른 사용자가 리뷰 수정 시도")
    void updateReview_OtherUser_Fails() {

        // given
        UUID anotherUserId = UUID.randomUUID();
        when(testUser.getId()).thenReturn(UUID.randomUUID());
        Review testReview = Review.create(4.0, "정말 좋은 책입니다.", testBook, testUser);

        when(reviewRepository.findById(testReviewId)).thenReturn(Optional.of(testReview)); // Review 조회 Mock

        // when & then
        assertThrows(ReviewAccessDeniedException.class, () -> {
            reviewService.updateReview(updateRequest, testUserId, testReviewId);
        });

        verify(reviewRepository, times(1)).findById(testReviewId);
        verifyNoMoreInteractions(reviewRepository); // 추가 호출 없음
    }

    @Test
    @DisplayName("실패: 논리 삭제된 리뷰는 수정 불가")
    void updateReview_DeletedReview_Fails() {
        // given
        Review testReview = Review.create(4.0, "정말 좋은 책입니다.", testBook, testUser);
        // 리뷰를 논리 삭제 상태로 전환
        testReview.delete();

        when(reviewRepository.findById(testReviewId))
                .thenReturn(Optional.of(testReview));

        // when & then
        assertThrows(ReviewNotFoundException.class, () -> {
            reviewService.updateReview(updateRequest, testUserId, testReviewId);
        });

        verify(reviewRepository).findById(testReviewId);
        verifyNoMoreInteractions(reviewRepository);
    }

    @Test
    @DisplayName("성공: 리뷰 논리 삭제")
    void softDeleteReview_Success() {
        // given
        Review mockReview = mock(Review.class);
        ReflectionTestUtils.setField(mockReview, "id", testReviewId);

        // Mock 동작 간소화
        when(mockReview.getUser()).thenReturn(testUser);
        when(mockReview.isDeleted()).thenReturn(false); // 초기 삭제 상태

        // 리포지토리 Mock 설정
        when(reviewRepository.findByIdAndIsDeletedFalse(testReviewId))
                .thenReturn(Optional.of(mockReview));

        // when
        reviewService.softDeleteReview(testReviewId, testUserId);

        // then
        verify(reviewRepository).findByIdAndIsDeletedFalse(testReviewId);
        verify(mockReview).delete();
        verify(reviewRepository).save(mockReview);
    }

    @Test
    @DisplayName("실패: 리뷰 논리 삭제 - 권한이 없는 사용자")
    void softDeleteReview_Fail_NoPermission() {
        // given
        Review mockReview = mock(Review.class);
        ReflectionTestUtils.setField(mockReview, "id", testReviewId);

        // Mock 동작 설정
        User anotherUser = mock(User.class);
        when(anotherUser.getId()).thenReturn(UUID.randomUUID());
        when(mockReview.getUser()).thenReturn(anotherUser);
        when(mockReview.isDeleted()).thenReturn(false);

        when(reviewRepository.findByIdAndIsDeletedFalse(testReviewId))
                .thenReturn(Optional.of(mockReview));

        // when & then
        assertThrows(ReviewAccessDeniedException.class, () ->
                reviewService.softDeleteReview(testReviewId, testUserId)
        );

        verify(reviewRepository).findByIdAndIsDeletedFalse(testReviewId);
        verify(mockReview, never()).delete();
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("성공: 리뷰 물리 삭제")
    void hardDeleteReview_Success() {
        // given
        Review testReview = Review.create(4.0, "정말 좋은 책입니다.", testBook, testUser);
        when(reviewRepository.findById(testReviewId))
                .thenReturn(Optional.of(testReview));
        // when
        reviewService.hardDeleteReview(testReviewId, testUserId);

        // then
        verify(reviewRepository).findById(testReviewId);
        verify(reviewRepository).delete(testReview);
    }

    @Test
    @DisplayName("실패: 이미 논리 삭제된 리뷰에 대한 물리 삭제")
    void hardDeleteReview_Fail_AlreadyDeleted() {
        // given
        Review mockReview = mock(Review.class);
        ReflectionTestUtils.setField(mockReview, "id", testReviewId);

        // 삭제된 상태 설정
        when(mockReview.isDeleted()).thenReturn(true);
        when(reviewRepository.findById(testReviewId))
                .thenReturn(Optional.of(mockReview));

        // when & then
        assertDoesNotThrow(() ->
                reviewService.hardDeleteReview(testReviewId,testUserId)
        );

        // 검증
        verify(reviewRepository).findById(testReviewId);
        verify(reviewRepository, times(1)).delete(mockReview);
    }

}
