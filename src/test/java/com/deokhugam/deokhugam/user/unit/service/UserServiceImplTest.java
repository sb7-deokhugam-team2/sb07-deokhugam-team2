package com.deokhugam.deokhugam.user.unit.service;

import com.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.domain.user.dto.response.UserDto;
import com.deokhugam.domain.user.entity.User;
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
import static org.mockito.Mockito.when;

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
        UUID userId = UUID.randomUUID();
        User user = new User("test@gmail.com", "testName", "1234");
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        //when
        UserDto userDto = userService.findUser(userId);

        //then
        assertThat(userDto.getEmail()).isEqualTo(user.getEmail());
        assertThat(userDto.getNickname()).isEqualTo(user.getNickname());
    }

    @Test
    void logicalDelete(){

    }

    @Test
    void updateNickname(){
        //given
//        UUID userId = UUID.randomUUID();
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("newName");
        User user = new User("test@gmail.com", "testName", "1234");
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(user));

        //when
        UserDto userDto = userService.updateNickname(UUID.randomUUID(), userUpdateRequest);

        //then
        assertThat(userDto.getEmail()).isEqualTo(user.getEmail());
        assertThat(userDto.getNickname()).isEqualTo(userUpdateRequest.nickname());

    }

    @Test
    void physicalDelete(){
        //given

        //when

        //then

    }
}
