package com.deokhugam.deokhugam.user.unit.domain;

import com.deokhugam.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User 테스트")
public class UserTest {

    @Test
    @DisplayName("updateNickname() 테스트")
    void updateNickname() {
        User user = User.create("test@gmail.com", "beforeName", "1234");
        String newName = "afterName";
        user.updateNickname(newName);

        assertThat(user.getNickname()).isEqualTo(newName);
    }
}
