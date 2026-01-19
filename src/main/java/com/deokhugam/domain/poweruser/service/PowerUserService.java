package com.deokhugam.domain.poweruser.service;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.poweruser.repository.PowerUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@RequiredArgsConstructor
public class PowerUserService {

    private final PowerUserRepository powerUserRepository;
    private final List<PeriodType> periodTypeList = List.of(PeriodType.values());

    public void calculateAndSaveRankings() {
        for (PeriodType periodType : periodTypeList) {
            calculateRankingByPeriod(periodType);
        }
    }

    private void calculateRankingByPeriod(PeriodType period) {
        powerUserRepository.updatePowerUserRanking(period);
    }
}
