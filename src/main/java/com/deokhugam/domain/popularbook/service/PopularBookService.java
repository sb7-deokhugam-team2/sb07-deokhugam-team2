package com.deokhugam.domain.popularbook.service;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.popularbook.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.popularbook.dto.response.CursorPageResponsePopularBookDto;

public interface PopularBookService {
    void snapshotPopularBooks(PeriodType periodType);

    CursorPageResponsePopularBookDto searchPopularBooks(PopularBookSearchCondition popularBookSearchCondition);

}
