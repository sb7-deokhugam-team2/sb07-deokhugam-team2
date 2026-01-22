package com.deokhugam.domain.popularbook.repository;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.popularbook.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.popularbook.dto.response.CursorResult;
import com.deokhugam.domain.popularbook.dto.response.PopularBookDto;
import com.deokhugam.domain.popularbook.dto.response.PopularBookAggregationDto;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface PopularBookRepositoryCustom {
    List<PopularBookAggregationDto> findTopPopularBookAggregates(PeriodType periodType, int limit);

    CursorResult<PopularBookDto> findTopPopularBooks(PopularBookSearchCondition condition, Instant windowStart, Pageable pageable);


}
