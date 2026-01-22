package com.deokhugam.deokhugam.notification.unit.service;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.notification.entity.Notification;
import com.deokhugam.domain.notification.repository.NotificationRepository;
import com.deokhugam.domain.notification.service.NotificationCreator;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationCreator 테스트")
class NotificationCreatorTest {

    @Mock
    NotificationRepository notificationRepository;

    @InjectMocks
    NotificationCreator notificationCreator;

    @Test
    @DisplayName("댓글 알림 성공")
    void createComment_success() {
        // given
        User user = User.create("test@gmail.com", "test", "12345678a!");
        Book book = Book.create("title", "author", "80", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(5.0, "content", book, user);
        Comment comment = Comment.create("content", user, review);

        // 저장될 내용을 가져옴
        ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);

        // when
        notificationCreator.createNotification(comment);

        // then
        verify(notificationRepository, times(1)).save(notificationArgumentCaptor.capture());

        Notification  notification = notificationArgumentCaptor.getValue();
        assertThat(notification.getContent()).isEqualTo("[test]님이 나의 리뷰에 댓글을 달았습니다.\n"+comment.getContent());
    }
}