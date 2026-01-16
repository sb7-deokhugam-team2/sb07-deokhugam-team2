package com.deokhugam.deokhugam.notification.integration.slice.jpa;

import com.deokhugam.domain.notification.repository.NotificationRepository;
import com.deokhugam.global.config.JpaAuditingConfig;
import com.deokhugam.global.config.QuerydslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
public class NotificationRepositoryTest {

    @Autowired
    NotificationRepository notificationRepository;
}
