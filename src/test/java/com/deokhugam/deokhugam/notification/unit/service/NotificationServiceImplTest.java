package com.deokhugam.deokhugam.notification.unit.service;


import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
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

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl 테스트")
public class NotificationServiceImplTest {

    @Mock
    NotificationRepository notificationRepository;

    @InjectMocks
    NotificationServiceImpl notificationService;

    @Test
    @DisplayName("알람 읽기 성공")
    void readNotification(){
        //given
        UUID userId = UUID.randomUUID();
        User user = User.create("test@gmail.com"," test123", "12345678a!");
        ReflectionTestUtils.setField(user, "id", userId);
        Book book = Book.create("title","author", "800", LocalDate.now(), "publisher", "", "description");
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
}
