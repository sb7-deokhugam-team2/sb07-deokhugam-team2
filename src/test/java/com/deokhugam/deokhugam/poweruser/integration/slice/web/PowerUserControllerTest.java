package com.deokhugam.deokhugam.poweruser.integration.slice.web;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.poweruser.controller.PowerUserController;
import com.deokhugam.domain.poweruser.dto.request.PowerUserSearchCondition;
import com.deokhugam.domain.poweruser.dto.response.CursorPageResponsePowerUserDto;
import com.deokhugam.domain.poweruser.dto.response.PowerUserDto;
import com.deokhugam.domain.poweruser.entity.PowerUser;
import com.deokhugam.domain.poweruser.enums.PowerUserDirection;
import com.deokhugam.domain.poweruser.service.PowerUserService;
import com.deokhugam.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PowerUserController.class)
public class PowerUserControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    PowerUserService powerUserService;

    @Test
    void getPowerUsers() throws Exception {
        User user = User.create("test@gmail.com", "test", "12121212qw!");
        User user2 = User.create("test2@gmail.com", "test2", "12121212qw!");
        User user3 = User.create("test3@gmail.com", "test3", "12121212qw!");
        PowerUser powerUser = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 1L, 100.0, 0L, 0L, 200.0, user);
        PowerUser powerUser2 = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 2L, 80.0, 0L, 0L, 160.0, user2);
        PowerUser powerUser3 = PowerUser.create(PeriodType.ALL_TIME, Instant.now(), 3L, 60.0, 0L, 0L, 120.0, user3);
        PowerUserDto from = PowerUserDto.from(powerUser);
        PowerUserDto from2 = PowerUserDto.from(powerUser2);
        PowerUserDto from3 = PowerUserDto.from(powerUser3);
        List<PowerUserDto> content = List.of(from, from2, from3);
        PowerUserSearchCondition condition = new PowerUserSearchCondition(PeriodType.ALL_TIME, PowerUserDirection.DESC, null, null, 10);
        CursorPageResponsePowerUserDto result = new CursorPageResponsePowerUserDto(content, null, null, 3, 3L, false);
        when(powerUserService.findPowerUsers(any(PowerUserSearchCondition.class)))
                .thenReturn(result);

        mockMvc.perform(get("/api/users/power")
                        .param("direction", "DESC")
                        .param("period", "ALL_TIME")
                        .param("limit", "10")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].score").value(100.0))
                .andExpect(jsonPath("$.totalElements").value(3L))
                .andExpect(jsonPath("$.hasNext").value(false));

        verify(powerUserService).findPowerUsers(any(PowerUserSearchCondition.class));
    }
}
