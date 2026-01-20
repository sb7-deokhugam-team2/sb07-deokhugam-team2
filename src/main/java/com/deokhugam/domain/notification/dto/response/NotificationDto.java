package com.deokhugam.domain.notification.dto.response;

import com.deokhugam.domain.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class NotificationDto {
    private UUID id;
    private UUID userId;
    private UUID reviewId;
    private String reviewTitle;
    private String content;
    private boolean confirmed;
    private Instant createdAt;
    private Instant updatedAt;

    public static NotificationDto from(Notification notification) {
        String reviewTitle;
        if (notification.getReview().getContent().length() > 21) {
            reviewTitle = notification.getReview().getContent().substring(0, 20) + "...";
        } else {
            reviewTitle = notification.getReview().getContent();
        }
        return new NotificationDto(
                notification.getId(),
                notification.getUser().getId(),
                notification.getReview().getId(),
                reviewTitle,
                notification.getContent(),
                notification.isConfirmed(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}
