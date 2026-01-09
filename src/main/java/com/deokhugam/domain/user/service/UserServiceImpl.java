package com.deokhugam.domain.user.service;

import com.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.domain.user.dto.response.UserDto;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.exception.UserNotFoundException;
import com.deokhugam.domain.user.repository.UserRepository;
import com.deokhugam.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Override
    public UserDto register(UserRegisterRequest userRegisterRequest) {
        /* 이메일, 닉네임, 패스워드 받고
        * 잘못된 요청 400에러, 이메일 중복 409에러
        * 이메일 중복 검증
        */
        if(userRepository.existsByEmail(userRegisterRequest.email())){
            throw new IllegalArgumentException("중복된 이메일입니다."); //	CONFLICT(409, Series.CLIENT_ERROR, "Conflict"),
        }

        User user = new  User(
                userRegisterRequest.email(),
                userRegisterRequest.nickname(),
                userRegisterRequest.password()
        );
        // userRepository.save();
        return null;
    }

    @Override
    public UserDto login(UserLoginRequest userLoginRequest) {
        return null;
    }

    @Override
    public UserDto findUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
        return UserDto.from(user);
    }

    @Override
    @Transactional
    public void logicalDelete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
        user.delete();
    }

    @Override
    @Transactional
    public UserDto updateNickname(UUID userId, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
        user.updateNickname(userUpdateRequest.nickname());
        return UserDto.from(user);
    }

    @Override
    public void physicalDelete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }
}
