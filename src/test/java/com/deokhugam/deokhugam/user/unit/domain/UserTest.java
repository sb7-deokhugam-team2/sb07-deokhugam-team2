package com.deokhugam.deokhugam.user.unit.domain;

import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.exception.UserEmailNotExistsException;
import com.deokhugam.domain.user.exception.UserEmailValidationException;
import com.deokhugam.domain.user.exception.UserNicknameValidationException;
import com.deokhugam.domain.user.exception.UserPasswordValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User 테스트")
public class UserTest {

    @Test
    @DisplayName("updateNickname() 테스트")
    void updateNickname() {
        User user = User.create("test@gmail.com", "beforeName", "12345678a!");
        String newName = "afterName";
        user.updateNickname(newName);

        assertThat(user.getNickname()).isEqualTo(newName);
    }

    @Test
    void validateEmail(){
        assertThatThrownBy(()->User.create("testgmail.com", "nickname", "qqqq1111!"))
                .isInstanceOf(UserEmailValidationException.class);
    }

    @Test
    void validateNickname(){
        assertThatThrownBy(()->User.create("test@gmail.com", "i", "qqqq1111!"))
                .isInstanceOf(UserNicknameValidationException.class);
    }

    @Test
    void validatePassword(){
        assertThatThrownBy(()->User.create("test@gmail.com", "nickname", "qqqq1111"))
                .isInstanceOf(UserPasswordValidationException.class);
    }
}
