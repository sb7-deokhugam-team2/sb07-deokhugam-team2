package com.deokhugam.deokhugam.notification.unit.service;


import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.domain.notification.dto.response.CursorPageResponseNotificationDto;
import com.deokhugam.domain.notification.dto.response.NotificationDto;
import com.deokhugam.domain.notification.entity.Notification;
import com.deokhugam.domain.notification.repository.NotificationRepository;
import com.deokhugam.domain.notification.service.NotificationServiceImpl;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.user.entity.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl 테스트")
public class NotificationServiceImplTest {

    @Mock
    NotificationRepository notificationRepository;

    @InjectMocks
    NotificationServiceImpl notificationService;

    @Test
    @DisplayName("알람 읽기 성공")
    void readNotification() {
        //given
        UUID userId = UUID.randomUUID();
        User user = User.create("test@gmail.com", " test123", "12345678a!");
        ReflectionTestUtils.setField(user, "id", userId);
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        UUID reviewId = UUID.randomUUID();
        ReflectionTestUtils.setField(review, "id", userId);
        Notification notification = Notification.create("test", review, user);
        when(notificationRepository.findWithUserAndReview(any(UUID.class)))
                .thenReturn(Optional.of(notification));
        NotificationUpdateRequest request = new NotificationUpdateRequest(true);

        //when
        NotificationDto notificationDto = notificationService.readNotification(UUID.randomUUID(), userId, request);

        //then
        assertThat(notificationDto.isConfirmed()).isTrue();
    }

    @Test
    @DisplayName("알람 모두 읽기 성공")
    void readNotifications() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create("test@gmail.com", " test123", "12345678a!");
        ReflectionTestUtils.setField(user, "id", userId);

        // when
        notificationService.readNotifications(user.getId());

        // then
        verify(notificationRepository, times(1)).readAllNotifications(userId);
    }

    @Test
    @DisplayName("알림 커서 페이지네이션 성공")
    void getNotifications() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create("test@gmail.com", " test123", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        UUID reviewId = UUID.randomUUID();

        String direction = "DESC";
        String cursor = null;
        Instant after = null;
        Integer limit = 20;

        List<Notification> notificationList = new ArrayList<>();
        Instant start = Instant.now();
        Instant last = Instant.now().minus(2, ChronoUnit.DAYS);
        for(int i = 21; i > 0; i--){
            Notification notification = Notification.create("test" + i, review, user);
            if(i==2){
                ReflectionTestUtils.setField(notification, "id", UUID.randomUUID());
                ReflectionTestUtils.setField(notification, "createdAt", last);
                ReflectionTestUtils.setField(notification, "updatedAt", last);
            } else {
                ReflectionTestUtils.setField(notification, "id", UUID.randomUUID());
                ReflectionTestUtils.setField(notification, "createdAt", start);
                ReflectionTestUtils.setField(notification, "updatedAt", start);
            }
            notificationList.add(notification);
        }
        NotificationSearchCondition searchCondition = new NotificationSearchCondition(userId, direction, cursor, after, limit);
        when(notificationRepository.searchNotifications(searchCondition)).thenReturn(notificationList);
        when(notificationRepository.countByUserId(userId)).thenReturn(20L);

        // when
        CursorPageResponseNotificationDto cursorPageResponseNotificationDto = notificationService.getNotifications(searchCondition);

        // then
        assertThat(cursorPageResponseNotificationDto.getContent().size()).isEqualTo(limit);
        assertThat(cursorPageResponseNotificationDto.isHasNext()).isTrue();
        assertThat(cursorPageResponseNotificationDto.getTotalElements()).isEqualTo(20L);
        assertThat(cursorPageResponseNotificationDto.getNextCursor()).isEqualTo(last.toString());
        assertThat(cursorPageResponseNotificationDto.getNextAfter()).isEqualTo(last);
    }

    @Test
    @DisplayName("읽은 알림 7일 후 삭제 성공")
    void deleteNotifications() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create("test@gmail.com", "test", "12345678a!");
        Book book = Book.create("title", "author", "800", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);

        Notification notification = Notification.create("content", review, user);
        notification.confirm();

        // when
        notificationService.deleteNotifications(Instant.now());

        // then
        verify(notificationRepository, times(1)).deleteOldConfirmedNotifications(any());
    }
}
