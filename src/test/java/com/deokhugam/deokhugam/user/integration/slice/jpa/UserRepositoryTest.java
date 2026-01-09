package com.deokhugam.deokhugam.user.integration.slice.jpa;

import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.config.JpaAuditingConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaAuditingConfig.class)
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    void existsByEmail(){

    }

    @Test
    void findByEmail(){

    }
}
