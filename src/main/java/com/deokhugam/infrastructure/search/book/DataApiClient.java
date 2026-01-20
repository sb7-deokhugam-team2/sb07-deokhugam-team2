package com.deokhugam.infrastructure.search.book;

import com.deokhugam.infrastructure.search.book.dto.BookGlobalApiDto;
import com.deokhugam.infrastructure.search.book.enums.ProviderType;

public interface DataApiClient {

    BookGlobalApiDto getData(String condition);

    ProviderType getProviderType();
}
