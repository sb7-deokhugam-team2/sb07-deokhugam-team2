package com.deokhugam.deokhugam.notification.integration.slice.jpa;

import com.deokhugam.domain.book.entity.Book;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.comment.entity.Comment;
import com.deokhugam.domain.comment.repository.CommentRepository;
import com.deokhugam.domain.notification.dto.request.NotificationSearchCondition;
import com.deokhugam.domain.notification.entity.Notification;
import com.deokhugam.domain.notification.repository.NotificationRepository;
import com.deokhugam.domain.review.entity.Review;
import com.deokhugam.domain.review.repository.ReviewRepository;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.config.JpaAuditingConfig;
import com.deokhugam.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
public class NotificationRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    EntityManagerFactory emf;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("알림 목록 커서 페이지네이션 성공")
    void searchNotification_cursor_desc() {
        // given
        User user = User.create("test@gmail.com", "test", "12345678a!");
        User savedUser = userRepository.save(user);
        Book book = Book.create("title", "author", "80", LocalDate.now(), "publisher", "", "description");
        Book savedBook = bookRepository.save(book);
        Review review = Review.create(5.0, "testReviewAuthor", savedBook, savedUser);
        Review savedReview = reviewRepository.save(review);
        Comment comment = Comment.create("content", savedUser, savedReview);
        Comment savedComment = commentRepository.save(comment);

        List<Notification> notificationList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Notification notification = Notification.create(user.getNickname() + "님이 댓글을 남겼습니다." + savedComment.getContent(), savedReview, savedUser);
            notificationList.add(notification);
        }
        notificationRepository.saveAll(notificationList);
        em.flush();
        em.clear();

        NotificationSearchCondition condition = new NotificationSearchCondition(
                savedUser.getId(),
                "DESC",
                null,
                null,
                20
        );

        // when
        List<Notification> result = notificationRepository.searchNotifications(condition);

        // then
        assertThat(result.size()).isEqualTo(condition.limit() + 1);
        assertThat(result.get(0).getCreatedAt()).isAfterOrEqualTo(result.get(result.size() - 1).getCreatedAt());
        assertThat(result).extracting(Notification::getCreatedAt).isSortedAccordingTo(Comparator.reverseOrder());
        assertThat(result).allSatisfy(notification -> {
            assertThat(notification.getUser().getId()).isEqualTo(savedUser.getId());
        });
    }

    @Test
    @DisplayName("Notification에서 user와 review를 페치 조인")
    void findWithUserAndReview() {
        // given
        User user = User.create("test@gmail.com", "test", "12345678a!");
        User savedUser = userRepository.save(user);
        Book book = Book.create("title", "author", "80", LocalDate.now(), "publisher", "", "description");
        Book savedBook = bookRepository.save(book);
        Review review = Review.create(5.0, "testReviewAuthor", savedBook, savedUser);
        Review savedReview = reviewRepository.save(review);
        Comment comment = Comment.create("content", savedUser, savedReview);
        Comment savedComment = commentRepository.save(comment);

        Notification notification = Notification.create(user.getNickname() + "님이 댓글을 남겼습니다." + savedComment.getContent(), savedReview, savedUser);
        Notification savedNotification = notificationRepository.save(notification);
        em.flush();
        em.clear();

        // when
        Notification findNotification = notificationRepository.findWithUserAndReview(savedNotification.getId()).orElse(null);

        // then
        boolean userIsLoaded = emf.getPersistenceUnitUtil().isLoaded(findNotification.getUser());
        boolean reviewIsLoaded = emf.getPersistenceUnitUtil().isLoaded(findNotification.getReview());
        assertThat(userIsLoaded).isTrue();
        assertThat(reviewIsLoaded).isTrue();
        assertThat(findNotification.getContent()).isEqualTo(savedNotification.getContent());
        assertThat(findNotification.getUser().getNickname()).isEqualTo(user.getNickname());
        assertThat(findNotification.getReview().getContent()).isEqualTo(review.getContent());

    }

    @Test
    @DisplayName("알림 일괄 읽음 처리 성공")
    void readAllNotification() {
        // given
        User user = User.create("test@gmail.com", "test", "12345678a!");
        User savedUser = userRepository.save(user);
        Book book = Book.create("title", "author", "80", LocalDate.now(), "publisher", "", "description");
        Book savedBook = bookRepository.save(book);
        Review review = Review.create(5.0, "testReviewAuthor", savedBook, savedUser);
        Review savedReview = reviewRepository.save(review);
        Comment comment = Comment.create("content", savedUser, savedReview);
        Comment savedComment = commentRepository.save(comment);

        List<Notification> notificationList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Notification notification = Notification.create(user.getNickname() + "님이 댓글을 남겼습니다." + savedComment.getContent(), savedReview, savedUser);
            notificationList.add(notification);
            notification.confirm();
        }
        notificationRepository.saveAll(notificationList);
        em.flush();
        em.clear();

        // when
        notificationRepository.readAllNotifications(review.getUser().getId());

        List<Notification> trueNotification = notificationList.stream()
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .toList();
        // then
        assertThat(trueNotification).hasSize(5);
        assertThat(trueNotification).extracting(Notification::isConfirmed).containsOnly(true);
    }

    @Test
    @DisplayName("1주일이 경과된 읽은 알림 삭제 성공")
    void deleteOldConfirmedNotifications() {
        // given
        User user = User.create("test@gmail.com", "test", "12345678a!");
        User savedUser = userRepository.save(user);
        Book book = Book.create("title", "author", "80", LocalDate.now(), "publisher", "", "description");
        Book savedBook = bookRepository.save(book);
        Review review = Review.create(5.0, "testReviewAuthor", savedBook, savedUser);
        Review savedReview = reviewRepository.save(review);
        Comment comment = Comment.create("content", savedUser, savedReview);
        Comment savedComment = commentRepository.save(comment);

        Notification deleteNotification = Notification.create("content", savedReview, savedUser);
        deleteNotification.confirm();
        notificationRepository.save(deleteNotification);

        Notification notDeleteNotification = Notification.create("content", savedReview, savedUser);
        notificationRepository.save(notDeleteNotification);

        em.flush();
        em.clear();

        // when
        Instant oneWeekLater = Instant.now().plus(7, ChronoUnit.DAYS);
        long deleteCount = notificationRepository.deleteOldConfirmedNotifications(oneWeekLater);

        // then
        assertThat(deleteCount).isEqualTo(1);
        assertThat(notificationRepository.findById(deleteNotification.getId())).isEmpty();
        assertThat(notificationRepository.findById(notDeleteNotification.getId())).isPresent();
    }

    @Test
    @DisplayName("totalElement를 위한 count쿼리 : 알림이 20개 일 때")
    void countByUserId_20L() {
        // given
        User user = User.create("test@gmail.com", "test", "12345678a!");
        User savedUser = userRepository.save(user);
        Book book = Book.create("title", "author", "80", LocalDate.now(), "publisher", "", "description");
        Book savedBook = bookRepository.save(book);
        Review review = Review.create(5.0, "testReviewAuthor", savedBook, savedUser);
        Review savedReview = reviewRepository.save(review);
        Comment comment = Comment.create("content", savedUser, savedReview);
        Comment savedComment = commentRepository.save(comment);

        List<Notification> notificationList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Notification notification = Notification.create(user.getNickname() + "님이 댓글을 남겼습니다." + savedComment.getContent(), savedReview, savedUser);
            notificationList.add(notification);
        }
        notificationRepository.saveAll(notificationList);
        em.flush();
        em.clear();

        // when
        long result = notificationRepository.countByUserId(savedUser.getId());

        // then
        assertThat(result).isEqualTo(20L);
    }

    @Test
    @DisplayName("totalElements를 위한 count쿼리 : 알림이 0개 일 때")
    void countByUserId_0L() {
        // given
        User user = User.create("test@gmail.com", "test", "12345678a!");
        User savedUser = userRepository.save(user);
        Book book = Book.create("title", "author", "80", LocalDate.now(), "publisher", "", "description");
        Book savedBook = bookRepository.save(book);
        Review review = Review.create(5.0, "testReviewAuthor", savedBook, savedUser);
        Review savedReview = reviewRepository.save(review);
        Comment comment = Comment.create("content", savedUser, savedReview);
        Comment savedComment = commentRepository.save(comment);
        em.flush();
        em.clear();

        // when
        long result = notificationRepository.countByUserId(savedUser.getId());

        // then
        assertThat(result).isEqualTo(0L);
    }
}
