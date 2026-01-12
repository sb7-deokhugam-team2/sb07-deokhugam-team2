package com.deokhugam.deokhugam.user.integration.slice.jpa;

import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.config.JpaAuditingConfig;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(JpaAuditingConfig.class)
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    void existsByEmail(){
        //given
        User user = new User("test@gmail.com", "nickname", "1234");
        userRepository.save(user);

        //when
        boolean result = userRepository.existsByEmail("test@gmail.com");

        //then
        assertThat(result).isTrue();
    }

    @Test
    void findByEmail(){
        //given
        User user = new User("test@gmail.com", "nickname", "1234");
        userRepository.save(user);

        //when
        User findUser = userRepository.findByEmail("test@gmail.com").orElseThrow(RuntimeException::new);

        //then
        assertThat(findUser).isEqualTo(user);

    }
}
