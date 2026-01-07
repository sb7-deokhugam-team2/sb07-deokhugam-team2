package com.deokhugam.domain.comment.entity;

import com.deokhugam.domain.base.BaseDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseDeletableEntity {
    @Column(name = "content", nullable = false, length = 500)
    private String content;
    // TODO: review , user 연관관계 필드 추가 필요
}
