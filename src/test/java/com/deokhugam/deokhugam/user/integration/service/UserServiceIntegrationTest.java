package com.deokhugam.deokhugam.user.integration.service;

import com.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.domain.user.dto.response.UserDto;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.exception.UserAlreadyExistsException;
import com.deokhugam.domain.user.exception.UserEmailNotExistsException;
import com.deokhugam.domain.user.exception.UserNotFoundException;
import com.deokhugam.domain.user.exception.UserPasswordException;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.domain.user.service.UserService;
import com.deokhugam.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void register_success() {
        //given
        User user = User.create("integration@gmail.com", "testName", "12345678a!");
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(user.getEmail(), user.getNickname(), user.getPassword());


        //when
        UserDto userDto = userService.register(userRegisterRequest);

        //then
        assertThat(userDto.getEmail()).isEqualTo(user.getEmail());
        assertThat(userDto.getNickname()).isEqualTo(user.getNickname());
    }

    @Test
    @DisplayName("회원가입 실패 테스트 : 이미 존재하는 이메일일 경우 UserAlreadyExistsException 실행")
    void register_fail() {
        // given
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest("testtest@gmail.com", "testName", "12345678a!");
        UserRegisterRequest userRegisterRequest2 = new UserRegisterRequest("testtest@gmail.com", "testName", "12345678a!");
        userService.register(userRegisterRequest);

        // when & then
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.register(userRegisterRequest2);
        });
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void login_success() {
        //given
        String email = "testtest@gamil.com";
        String password = "12345678a!";
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(email, "testName", password);
        userService.register(userRegisterRequest);


        UserLoginRequest userLoginRequest = new UserLoginRequest(email, password);

        //when
        UserDto userDto = userService.login(userLoginRequest);

        //then
        assertThat(userDto.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("로그인 실패 테스트 : 1. 이메일이 틀릴 경우 UserEmailNotExistsException 실행")
    void login_fail_email() {
        // given
        String email = "testtest@gamil.com";
        String password = "12345678a!";
        UserLoginRequest userLoginRequest = new UserLoginRequest(email, password);

        // when & then
        UserEmailNotExistsException exception = assertThrows(UserEmailNotExistsException.class, () -> {
            userService.login(userLoginRequest);
        });
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_EMAIL_NOT_EXISTS);
    }

    @Test
    @DisplayName("로그인 실패 테스트 : 2. 비밀번호가 일치하지 않을 경우 UserPasswordException 실행")
    void login_fail_password() {
        // given
        String email = "testtest@gamil.com";
        String password = "12345678a!";
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(email, "testName", password);
        userService.register(userRegisterRequest);

        UserLoginRequest userLoginRequest = new UserLoginRequest(email, "wrong_password1!");

        // when & then
        UserPasswordException exception = assertThrows(UserPasswordException.class, () -> {
            userService.login(userLoginRequest);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_PASSWORD_NOT_EQUAL);
    }

    @Test
    void findUser() {
        //given
        String email = "testtest@gamil.com";
        String password = "12345678a!";
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(email, "testName", password);
        UserDto user = userService.register(userRegisterRequest);

        //when
        UserDto userDto = userService.findUser(user.getId());

        //then
        assertThat(userDto.getEmail()).isEqualTo(user.getEmail());
        assertThat(userDto.getNickname()).isEqualTo(user.getNickname());
    }

    @Test
    @DisplayName("논리 삭제 성공")
    void logicalDelete() {
        //given
        String email = "testtest@gamil.com";
        String password = "12345678a!";
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(email, "testName", password);
        UserDto user = userService.register(userRegisterRequest);

        //when
        userService.logicalDelete(user.getId());

        //then
        assertThatThrownBy(() -> userService.findUser(user.getId()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("논리 삭제 실패: 유저가 없으면 UserNotFoundException 발생")
    void logicalDelete_not_foud() {
        //given

        //when&then
        assertThatThrownBy(() -> userService.logicalDelete(UUID.randomUUID()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("수정 성공")
    void updateNickname() {
        //given
        String email = "testtest@gamil.com";
        String password = "12345678a!";
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(email, "testName", password);
        UserDto user = userService.register(userRegisterRequest);
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("newName");

        //when
        UserDto userDto = userService.updateNickname(user.getId(), userUpdateRequest);

        //then
        assertThat(userDto.getEmail()).isEqualTo(user.getEmail());
        assertThat(userDto.getNickname()).isEqualTo(userUpdateRequest.nickname());
    }

    @Test
    @DisplayName("수정 실패: 유저가 없으면 UserNotFoundException 발생")
    void updateNickname_not_found() {
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("newName");

        // When & Then
        assertThatThrownBy(() -> userService.updateNickname(UUID.randomUUID(), userUpdateRequest))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("물리 삭제 성공")
    void physicalDelete() {
        //given
        String email = "testtest@gamil.com";
        String password = "12345678a!";
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(email, "testName", password);
        UserDto user = userService.register(userRegisterRequest);

        //when
        userService.physicalDelete(user.getId());

        //then
        assertThatThrownBy(()->userService.findUser(user.getId()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("물리 삭제 실패: 유저가 없으면 UserNotFoundException 발생")
    void physicalDelete_not_found() {
        // When & Then
        assertThatThrownBy(() -> userService.physicalDelete(UUID.randomUUID()))
                .isInstanceOf(UserNotFoundException.class);
    }
}

