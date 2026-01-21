package com.deokhugam.deokhugam.notification.integration.service;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.comment.repository.CommentRepository;
import com.deokhugam.domain.comment.service.CommentService;
import com.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.domain.notification.dto.response.NotificationDto;
import com.deokhugam.domain.notification.entity.Notification;
import com.deokhugam.domain.notification.repository.NotificationRepository;
import com.deokhugam.domain.notification.service.NotificationService;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@DisplayName("알림 서비스 통합 테스트")
public class NotificationServiceIntegrationTest {
    @Autowired
    CommentService commentService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("알림 생성 및 단건 읽기")
    void createNotificationAndRead() {
        // given 댓글 작성-> 알림 생성 확인 -> 알림 단건 읽기
        User user = User.create("test5@gmail.com", "test", "12345678a!");
        Book book = Book.create("title", "author", "80", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        Comment comment = Comment.create("댓글", user, review);
        userRepository.save(user);
        bookRepository.save(book);
        reviewRepository.save(review);
        em.flush();
        em.clear();

        // when
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(review.getId(), user.getId(), "댓글");
        commentService.createComment(commentCreateRequest);

        List<Notification> notifications = notificationRepository.findAll();
        Notification notification = notifications.get(0);
        NotificationUpdateRequest notificationUpdateRequest = new NotificationUpdateRequest(true);
        NotificationDto notificationDto = notificationService.readNotification(notification.getId(), notification.getUser().getId(), notificationUpdateRequest);

        // then
        assertThat(notificationDto.getContent()).contains("test", comment.getContent());
        assertThat(notificationDto.getUserId()).isEqualTo(review.getUser().getId());
        assertThat(notificationDto.isConfirmed()).isTrue();
    }

    @Test
    @DisplayName("알림 일괄 읽음 테스트")
    void readAllNotifications() {
        // given
        User user = User.create("test5@gmail.com", "test", "12345678a!");
        Book book = Book.create("title", "author", "80", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        userRepository.save(user);
        bookRepository.save(book);
        reviewRepository.save(review);

        for (int i = 0; i < 3; i++) {
            CommentCreateRequest commentCreateRequest = new CommentCreateRequest(review.getId(), user.getId(), "댓글" + i);
            commentService.createComment(commentCreateRequest);
        }

        em.flush();
        em.clear();

        // when
        notificationService.readNotifications(user.getId());
        List<Notification> notifications = notificationRepository.findAll();

        // then
        assertThat(notifications.size()).isEqualTo(3);
        for (Notification notification : notifications) {
            assertThat(notification.isConfirmed()).isTrue();
        }
    }

    @Test
    @DisplayName("읽은 지 7일 경과한 알림 삭제")
    void deleteNotification_weekLater() {
        // given
        User user = User.create("test5@gmail.com",  "test", "12345678a!");
        Book book = Book.create("title", "author", "80", LocalDate.now(), "publisher", "", "description");
        Review review = Review.create(4.0, "content", book, user);
        userRepository.save(user);
        bookRepository.save(book);
        reviewRepository.save(review);

        Notification notification1 = Notification.create("content", review, user);
        notification1.confirm();
        notificationRepository.save(notification1);

        Notification notification2 = Notification.create("content", review, user);
        notificationRepository.save(notification2);

        em.flush();
        em.clear();

        // when
        Instant weekLater = Instant.now().plus(7, ChronoUnit.DAYS);
        notificationService.deleteNotifications(weekLater);

        // then
        assertThat(notificationRepository.findAll().size()).isEqualTo(1);
        assertThat(notificationRepository.findById(notification1.getId())).isEmpty();
        assertThat(notificationRepository.findById(notification2.getId())).isPresent();
    }
}
