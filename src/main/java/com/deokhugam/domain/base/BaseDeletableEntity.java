package com.deokhugam.domain.base;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseDeletableEntity extends BaseUpdateEntity {
    private boolean isDeleted;

    public void delete() {
        isDeleted = true;
    }
    public void restore() {
        isDeleted = false;
    }
}
