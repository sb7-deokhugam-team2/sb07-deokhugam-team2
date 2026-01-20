package com.deokhugam.domain.poweruser.controller;

import com.deokhugam.domain.poweruser.dto.request.PowerUserSearchCondition;
import com.deokhugam.domain.poweruser.service.PowerUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PowerUserController {

    private final PowerUserService powerUserService;

    @GetMapping("/api/users/power")
    public void getPowerUsers(
            @Valid @ModelAttribute PowerUserSearchCondition condition){
//        powerUserService
    }
}
