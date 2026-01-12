package com.deokhugam.deokhugam.user.integration.slice.web;

import com.deokhugam.domain.user.controller.UserController;
import com.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.deokhugam.domain.user.dto.response.UserDto;
import com.deokhugam.domain.user.entity.User;
import com.deokhugam.domain.user.service.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    UserServiceImpl userService;

//    @Test
//    void register(){
//        //given
//
//        //when&then
//    }

//    @Test
//    void login(){
//        //given
//
//        //when&then
//    }

    @Test
    @DisplayName("유저 조회 성공")
    void findUser() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        User user = User.create("test@gmail.com", "test", "12345678a!");
        UserDto userDto = UserDto.from(user);
        when(userService.findUser(any()))
                .thenReturn(userDto);

        //when&then
        mockMvc.perform(get("/api/users/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.nickname").value(user.getNickname()));

        verify(userService).findUser(any());
    }

    @Test
    @DisplayName("유저 논리 삭제 성공")
    void logicalDelete() throws Exception {
        //given
        UUID userId = UUID.randomUUID();

        //when&then
        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andDo(print())
                .andExpect(status().isNoContent());
        verify(userService).logicalDelete(userId);
    }

    @Test
    @DisplayName("유저 수정 성공")
    void updateUser() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        User user = User.create("test@gmail.com", "newName", "12345678a!");
        UserDto userDto = UserDto.from(user);
        when(userService.updateNickname(any(UUID.class), any(UserUpdateRequest.class)))
                .thenReturn(userDto);

        //when&then
        mockMvc.perform(patch("/api/users/{userId}", userId)
                        .param("nickname", "newName")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("newName"));
        verify(userService).updateNickname(any(UUID.class), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("유저 물리 삭제 성공")
    void physicalDelete() throws Exception {
        //given
        UUID userId = UUID.randomUUID();

        //when&then
        mockMvc.perform(delete("/api/users/{userId}/hard", userId))
                .andDo(print())
                .andExpect(status().isNoContent());
        verify(userService).physicalDelete(userId);
    }
}
