package com.deokhugam.domain.popularbook.service;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.popularbook.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.popularbook.dto.response.CursorPageResponsePopularBookDto;
import org.springframework.stereotype.Service;

@Service
public class PopularBookServiceImpl implements PopularBookService {

    @Override
    public void recalculate(PeriodType periodType) {
        switch (periodType) {
            case DAILY:
                break;
            case WEEKLY:
                break;
            case MONTHLY:
                break;
            case ALL_TIME:
                break;
            default:
                break;
        }
    }

    @Override
    public CursorPageResponsePopularBookDto searchPopularBooks(PopularBookSearchCondition popularBookSearchCondition) {
        return null;
    }
}

