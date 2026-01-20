package com.deokhugam.domain.notification.service;

import com.deokhugam.domain.comment.dto.response.CommentDto;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.notification.dto.response.NotificationDto;
import com.deokhugam.domain.notification.entity.Notification;
import com.deokhugam.domain.notification.repository.NotificationRepository;
import com.deokhugam.domain.review.dto.response.ReviewDto;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationCreator {

    private final NotificationRepository notificationRepository;
    // 다른 곳에서 호출하여 사용
        /* 알림이 오는 경우
        1. 좋아요를 눌렀을 때 : [00]님이 나의 리뷰를 좋아합니다.\n + review.getContent
        2. 댓글이 달렸을 때 : [00]님이 나의 리뷰에 댓글을 달았습니다.\n + comment.getContent
        3. 내가 작성한 리뷰가 순위권에 들었을 때 :
         */

    public void createNotification(Comment comment) {

        Notification notification = Notification.create(
                "[" + comment.getUser().getNickname() + "]님이 나의 리뷰에 댓글을 달았습니다.\n" + comment.getContent(),
                comment.getReview(),
                comment.getUser()
        );
        notificationRepository.save(notification);
    }
}
