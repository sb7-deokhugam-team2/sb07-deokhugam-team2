package com.deokhugam.domain.popularbook.controller;

import com.deokhugam.domain.popularbook.controller.docs.PopularBookControllerDocs;
import com.deokhugam.domain.popularbook.dto.request.PopularBookSearchCondition;
import com.deokhugam.domain.popularbook.dto.response.CursorPageResponsePopularBookDto;
import com.deokhugam.domain.popularbook.service.PopularBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class PopularBookController implements PopularBookControllerDocs {
    private final PopularBookService popularBookService;

    @GetMapping("/popular")
    @Override
    public ResponseEntity<CursorPageResponsePopularBookDto> getPopularBooks(
            PopularBookSearchCondition popularBookSearchCondition
    ) {
        CursorPageResponsePopularBookDto cursorPageResponsePopularBookDto = popularBookService.searchPopularBooks(popularBookSearchCondition);
        return ResponseEntity.ok(cursorPageResponsePopularBookDto);
    }

}
