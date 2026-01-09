package com.deokhugam.domain.user.service;

import com.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.domain.user.dto.response.UserDto;

import java.util.UUID;

public interface UserService {
    UserDto register(UserRegisterRequest userRegisterRequest);
    UserDto login(UserLoginRequest userLoginRequest);
    UserDto findUser(UUID userId);
    void logicalDelete(UUID userId);
    UserDto updateNickname(UserUpdateRequest userUpdateRequest);
    void physicalDelete(UUID userId);
}
