package com.deokhugam.domain.book.service.impl;

import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponsePopularBookDto;
import com.deokhugam.domain.book.dto.response.NaverBookDto;
import com.deokhugam.domain.book.repository.BookRepository;
import com.deokhugam.domain.book.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;


    @Override
    public BookDto getBookDetail(UUID bookId) {
        // TODO: 26. 1. 9. 도서 ID로 해당 도서의 상세 정보 조회 로직
        return null;
    }

    @Override
    public List<CursorPageResponseBookDto> searchBook(BookSearchCondition bookSearchCondition) {
        // TODO: 26. 1. 9. Cursor검색(QueryDSL)을 통한 검색 데이터 반환 로직 
        return List.of();
    }

    @Override
    public List<CursorPageResponsePopularBookDto> searchPopularBook(PopularBookSearchCondition popularBookSearchCondition) {
        // TODO: 26. 1. 9. 인기 도서 미구현으로 틀 성생 추후 로직 구현(해당 기능 popularBook 이전 고려 필요)
        return List.of();
    }

    @Override
    public NaverBookDto getBookByIsbn(String isbn) {
        
        return null;
    }

    @Override
    public String extractIsbnFromImage(MultipartFile image) {
        return "";
    }

    @Override
    public BookDto createBook(BookCreateRequest bookCreateRequest, MultipartFile thumbnail) {
        return null;
    }

    @Override
    public BookDto updateBook(UUID bookId, BookCreateRequest bookCreateRequest, MultipartFile thumbnail) {
        return null;
    }

    @Override
    public void softDeleteBook(UUID bookId) {
        // TODO: 26. 1. 9. 명확한 구분을 위해 soft, hard로 구분 상의 필요  
    }

    @Override
    public void hardDeleteBook(UUID bookId) {

    }
}
