package com.deokhugam.domain.user.service;

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
        if(userRepository.existsByEmail(userRegisterRequest.email())){
            throw new UserAlreadyExistsException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        User user = User.create(
                userRegisterRequest.email(),
                userRegisterRequest.nickname(),
                userRegisterRequest.password()
        );

        User saved = userRepository.save(user);

        return UserDto.from(saved);
    }

    @Override
    public UserDto login(UserLoginRequest userLoginRequest) {
        User user = userRepository.findByEmail(userLoginRequest.email())
                .orElseThrow(() -> new UserEmailNotExistsException(ErrorCode.USER_EMAIL_NOT_EXISTS));

        if(!user.getPassword().equals(userLoginRequest.password())){
            throw new UserPasswordException(ErrorCode.USER_PASSWORD_NOT_EQUAL);
        }

        return UserDto.from(user);
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
