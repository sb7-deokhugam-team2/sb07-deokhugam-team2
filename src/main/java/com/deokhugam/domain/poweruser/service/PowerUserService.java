package com.deokhugam.domain.poweruser.service;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.poweruser.dto.request.PowerUserSearchCondition;
import com.deokhugam.domain.poweruser.dto.response.CursorPageResponsePowerUserDto;

import java.time.ZonedDateTime;

public interface PowerUserService {
    CursorPageResponsePowerUserDto findPowerUsers(PowerUserSearchCondition condition);
    void calculateRankingByPeriod(PeriodType period, ZonedDateTime time);
}
