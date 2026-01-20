package com.deokhugam.domain.popularbook.repository;

import com.deokhugam.domain.base.PeriodType;
import com.deokhugam.domain.book.dto.response.PopularBookDto;
import com.deokhugam.domain.popularbook.dto.response.PopularBookAggregationDto;

import java.time.Instant;
import java.util.List;

public interface PopularBookRepositoryCustom {
    List<PopularBookAggregationDto> findTopPopularBookAggregates(PeriodType periodType, int limit);
    List<PopularBookDto> findTopPopularBooks(PeriodType periodType, int limit, Instant windowStart);



}
