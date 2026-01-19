package com.deokhugam.deokhugam.notification.integration.slice.web;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.notification.controller.NotificationController;
import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.domain.notification.dto.response.CursorPageResponseNotificationDto;
import com.deokhugam.domain.notification.dto.response.NotificationDto;
import com.deokhugam.domain.notification.entity.Notification;
import com.deokhugam.domain.notification.service.NotificationService;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
public class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    NotificationService notificationService;

    @Test
    @DisplayName("알림 읽음 상태 업데이트")
    void readNotification() throws Exception {
        // given
        User user = User.create("test@gmail.com", "test", "12345678a!");
        Book book = Book.create("title", "author", "80", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        UUID userId = UUID.randomUUID();
        Notification notification = Notification.create("content", review, user);
        UUID reviewId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        NotificationDto notificationDto = new NotificationDto(notificationId, userId, reviewId, review.getContent(), "content", true, Instant.now(), Instant.now());
        NotificationUpdateRequest notificationUpdateRequest = new NotificationUpdateRequest(true);

        when(notificationService.readNotification(any(UUID.class), any(UUID.class), any(NotificationUpdateRequest.class)))
                .thenReturn(notificationDto);

        // when & then
        mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
                        .header("Deokhugam-Request-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationUpdateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmed").value(true));

        verify(notificationService).readNotification(eq(notificationId), eq(userId), any(NotificationUpdateRequest.class));
    }

    @Test
    @DisplayName("모든 알림 읽음 처리")
    void readAll() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        // when & then
        mockMvc.perform(patch("/api/notifications/read-all")
                        .header("Deokhugam-Request-User-ID", userId.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).readNotifications(userId);
    }

    @Test
    @DisplayName("알림 목록 조회")
    void getNotification() throws Exception {
        // given
        User user = User.create("test@gmail.com", "test", "12345678a!");
        Book book = Book.create("title", "author", "80", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        UUID userId = UUID.randomUUID();

        List<NotificationDto> notificationList = new ArrayList<>();
        Notification notification = Notification.create("content", review, user);
        NotificationDto notificationDto = NotificationDto.from(notification);
        notificationList.add(notificationDto);

        CursorPageResponseNotificationDto cursorPageResponseNotificationDto
                = new CursorPageResponseNotificationDto(
                notificationList, Instant.now().toString(), Instant.now(), 20, 100L, false
        );
        when(notificationService.getNotifications(any(NotificationSearchCondition.class))).thenReturn(cursorPageResponseNotificationDto);

        // when & then
        mockMvc.perform(get("/api/notifications")
                        .param("userId", userId.toString())
                        .param("direction", "DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(100))
                .andExpect(jsonPath("$.hasNext").value(false));

        verify(notificationService, times(1)).getNotifications(any(NotificationSearchCondition.class));
    }

}
