package com.deokhugam.domain.notification.controller;

import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.domain.notification.dto.response.CursorPageResponseNotificationDto;
import com.deokhugam.domain.notification.dto.response.NotificationDto;
import com.deokhugam.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PatchMapping("/{notificationId}")
    public ResponseEntity<NotificationDto> readNotification(
            @PathVariable UUID notificationId,
            @RequestHeader("Deokhugam-Request-Id") UUID userId,
            @RequestBody NotificationUpdateRequest notificationUpdateRequest
    ){
        return null;
    }
    @PatchMapping("/read-all")
    public ResponseEntity<Void>  readAll(
            @RequestHeader("Deokhugam-Request-Id") UUID userId
    ){
        return null;
    }

    @GetMapping
    public ResponseEntity<CursorPageResponseNotificationDto> getNotification(
            @ModelAttribute NotificationSearchCondition condition
    ){
        CursorPageResponseNotificationDto cursorPageResponseNotificationDto
                = notificationService.getNotifications(condition);
        return ResponseEntity.ok().body(cursorPageResponseNotificationDto);
    }
}
