package com.deokhugam.domain.user.entity;

import com.deokhugam.domain.base.BaseDeletableEntity;
import com.deokhugam.domain.user.exception.UserPasswordValidationException;
import com.deokhugam.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseDeletableEntity {
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "password", nullable = false)
    private String password;

    private User(String email, String nickname, String password) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
    }

    public static User create(String email, String nickname, String password) {
        validateEmail(email);
        validateNickname(nickname);
        validatePassword(password);
        return new User(email, nickname, password);
    }

    public void updateNickname(String newNickname) {
        validateNickname(newNickname);
        this.nickname = newNickname;
    }

    private static void validatePassword(String password) {
        if (password != null) {
            String pwdRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$";
            if (password.matches(pwdRegex)) {
                return;
            }
        }
        throw new UserPasswordValidationException(ErrorCode.USER_PASSWORD_VALIDATION);
    }

    private static void validateNickname(String nickname) {
        if (nickname == null || nickname.length() < 2) {
            throw new UserPasswordValidationException(ErrorCode.USER_NICKNAME_VALIDATION);
        }
    }

    private static void validateEmail(String email) {
        if (!email.contains("@")) {
            throw new UserPasswordValidationException(ErrorCode.USER_EMAIL_VALIDATION);
        }
    }
}
