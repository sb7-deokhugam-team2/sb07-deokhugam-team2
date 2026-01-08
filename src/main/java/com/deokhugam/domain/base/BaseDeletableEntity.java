package com.deokhugam.domain.base;

ㅇimport jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@MappedSuperclass
public abstract class BaseDeletableEntity extends BaseUpdateEntity {
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public void delete() {
        isDeleted = true;
    }
    public void restore() {
        isDeleted = false;
    }
}
