package com.deokhugam.domain.user.controller;

import com.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.domain.user.dto.response.UserDto;
import com.deokhugam.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
            @Valid @RequestBody UserRegisterRequest userRegisterRequest
    ) {
        UserDto userDto = userService.register(userRegisterRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(
            @Valid @RequestBody UserLoginRequest userLoginRequest
    ) {
        UserDto userDto = userService.login(userLoginRequest);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> findUser(@PathVariable UUID userId) {
        UserDto userDto = userService.findUser(userId);
        return ResponseEntity.ok().body(userDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> logicalDelete(@PathVariable UUID userId) {
        userService.logicalDelete(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID userId,
            @Valid @ModelAttribute UserUpdateRequest userUpdateRequest
    ) {
        UserDto userDto = userService.updateNickname(userId, userUpdateRequest);
        return ResponseEntity.ok().body(userDto);
    }

    @DeleteMapping("/{userId}/hard")
    public ResponseEntity<Void> physicalDelete(@PathVariable UUID userId) {
        userService.physicalDelete(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
