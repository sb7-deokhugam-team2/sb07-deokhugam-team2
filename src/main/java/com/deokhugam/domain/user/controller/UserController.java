package com.deokhugam.domain.user.controller;

import com.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.domain.user.dto.response.UserDto;
import com.deokhugam.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> register(
            UserRegisterRequest userRegisterRequest
    ) {
        return null;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login() {
        return null;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> findUser(@PathVariable UUID userId) {
        return null;
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> logicalDelete(@PathVariable UUID userId) {
        return null;
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID userId,
            @ModelAttribute UserUpdateRequest userUpdateRequest
            ) {
        return null;
    }

    @DeleteMapping("/{userId}/hard")
    public ResponseEntity<Void> physicalDelete(@PathVariable UUID userId) {
        return null;
    }
}
