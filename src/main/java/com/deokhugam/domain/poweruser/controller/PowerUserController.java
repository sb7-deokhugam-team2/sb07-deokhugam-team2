package com.deokhugam.domain.poweruser.controller;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.poweruser.controller.docs.PowerUserControllerDocs;
import com.deokhugam.domain.poweruser.dto.request.PowerUserSearchCondition;
import com.deokhugam.domain.poweruser.dto.response.CursorPageResponsePowerUserDto;
import com.deokhugam.domain.poweruser.scheduler.PowerUserScheduler;
import com.deokhugam.domain.poweruser.service.PowerUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PowerUserController implements PowerUserControllerDocs {

    private final PowerUserService powerUserService;
    private final PowerUserScheduler powerUserScheduler;

    @GetMapping("/api/users/power")
    public ResponseEntity<CursorPageResponsePowerUserDto> getPowerUsers(
            @Valid @ModelAttribute PowerUserSearchCondition condition){
        CursorPageResponsePowerUserDto powerUsers = powerUserService.findPowerUsers(condition);
        return ResponseEntity.ok().body(powerUsers);
    }

    @GetMapping("/api/users/batch")
    public void start(){
       powerUserScheduler.startRankingCalculate();
    }
}
