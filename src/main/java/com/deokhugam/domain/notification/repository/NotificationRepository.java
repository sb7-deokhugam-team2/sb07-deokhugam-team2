package com.deokhugam.domain.notification.repository;

import com.deokhugam.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationCustomRepository {
}
