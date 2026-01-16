package com.deokhugam.infrastructure.search.book;

import com.deokhugam.infrastructure.search.book.dto.BookGlobalApiDto;
import com.deokhugam.infrastructure.search.book.enums.ProviderType;
import com.deokhugam.infrastructure.search.book.exception.BookNotFoundInApi;
import com.deokhugam.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BookApiManager {
    private final Map<ProviderType, DataApiClient> clients;

    public BookApiManager(List<DataApiClient> clientList) {
        this.clients = clientList.stream().collect(Collectors.toMap(DataApiClient::getProviderType, Function.identity()));
    }

    public BookGlobalApiDto searchWithFallback(String isbn) {
        // 순서 지정
        List<ProviderType> priority = List.of(ProviderType.NAVER);

        for (ProviderType type : priority) {
            DataApiClient client = clients.get(type);
            if (client != null) {
                BookGlobalApiDto result = client.getData(isbn);
                if (result != null) return result;
            }
        }
        throw new BookNotFoundInApi(ErrorCode.BOOK_NOT_FOUND_IN_API);
    }
}
