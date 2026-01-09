package com.deokhugam.domain.user.service;

import com.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.domain.user.dto.response.UserDto;
import com.deokhugam.domain.user.entity.User;
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
