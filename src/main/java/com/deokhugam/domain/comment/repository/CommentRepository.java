package com.deokhugam.domain.comment.repository;

import com.deokhugam.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentCustomRepository {
    @Query("select c from Comment c join fetch c.user where c.id=:id")
    Optional<Comment> findWithUser(@Param("id") UUID id);

    @Query("select c" +
            " from Comment c" +
            " join fetch c.user" +
            " join fetch c.review" +
            " where c.id=:id")
    Optional<Comment> findWithUserAndReview(@Param("id") UUID id);
}
