package com.deokhugam.deokhugam.user.unit.service;

import com.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.domain.user.dto.response.UserDto;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.exception.UserNotFoundException;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.domain.user.service.UserService;
import com.deokhugam.domain.user.service.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
public class UserServiceImplTest {
    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserServiceImpl userService;

    @Test
    void register(){
        //given

        //when

        //then

    }

    @Test
    void login(){
        //given

        //when

        //then

    }

    @Test
    void findUser(){
        //given
        User user = new User("test@gmail.com", "testName", "1234");
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(user));

        //when
        UserDto userDto = userService.findUser(UUID.randomUUID());

        //then
        assertThat(userDto.getEmail()).isEqualTo(user.getEmail());
        assertThat(userDto.getNickname()).isEqualTo(user.getNickname());
    }

    @Test
    @DisplayName("논리 삭제 성공")
    void logicalDelete(){
        //given
        User user = new User("test@gmail.com", "testName", "1234");
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(user));

        //when
        userService.logicalDelete(UUID.randomUUID());

        //then
        verify(userRepository, times(1)).findById(any(UUID.class));
        assertThat(user.isDeleted()).isEqualTo(true);
    }

    @Test
    @DisplayName("논리 삭제 실패: 유저가 없으면 UserNotFoundException 발생")
    void logicalDelete_not_foud(){
        //given
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        //when
        assertThatThrownBy(() -> userService.logicalDelete(UUID.randomUUID()))
                .isInstanceOf(UserNotFoundException.class);

        //then
        verify(userRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    @DisplayName("수정 성공")
    void updateNickname(){
        //given
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("newName");
        User user = new User("test@gmail.com", "testName", "1234");
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(user));

        //when
        UserDto userDto = userService.updateNickname(UUID.randomUUID(), userUpdateRequest);

        //then
        assertThat(userDto.getEmail()).isEqualTo(user.getEmail());
        assertThat(userDto.getNickname()).isEqualTo(userUpdateRequest.nickname());
        verify(userRepository).findById(any(UUID.class));
    }

    @Test
    @DisplayName("수정 실패: 유저가 없으면 UserNotFoundException 발생")
    void updateNickname_not_found() {
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateNickname(UUID.randomUUID(), any(UserUpdateRequest.class)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("물리 삭제 성공")
    void physicalDelete(){
        //given
        User user = new User("test@gmail.com", "testName", "1234");
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(user));

        //when
        userService.physicalDelete(UUID.randomUUID());

        //then
        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    @DisplayName("물리 삭제 실패: 유저가 없으면 UserNotFoundException 발생")
    void physicalDelete_not_found() {
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.physicalDelete(UUID.randomUUID()))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).delete(any(User.class));
    }
}
