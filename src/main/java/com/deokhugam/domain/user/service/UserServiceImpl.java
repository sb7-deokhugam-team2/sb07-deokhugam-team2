package com.deokhugam.domain.user.service;

import com.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.domain.user.dto.response.UserDto;
import com.deokhugam.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Override
    public UserDto register(UserRegisterRequest userRegisterRequest) {
        return null;
    }

    @Override
    public UserDto login(UserLoginRequest userLoginRequest) {
        return null;
    }

    @Override
    public UserDto findUser(UUID userId) {
        return null;
    }

    @Override
    public void logicalDelete(UUID userId) {

    }

    @Override
    public UserDto updateNickname(UserUpdateRequest userUpdateRequest) {
        return null;
    }

    @Override
    public void physicalDelete(UUID userId) {

    }
}
