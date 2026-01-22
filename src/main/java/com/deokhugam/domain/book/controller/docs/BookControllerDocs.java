package com.deokhugam.domain.book.controller.docs;

import com.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.deokhugam.domain.book.dto.response.BookDto;
import com.deokhugam.domain.book.dto.response.CursorPageResponseBookDto;
import com.deokhugam.domain.book.dto.response.NaverBookDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "Book API", description = "도서 관리 관련 API (조회, 생성, 수정, 삭제, OCR, 네이버 검색)")
public interface BookControllerDocs {

    @Operation(summary = "도서 목록 조회 (커서 페이징)", description = "조건에 맞는 도서 목록을 커서 페이징 방식으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<CursorPageResponseBookDto> getAllBooks(
            @ModelAttribute BookSearchCondition searchCondition
    );

    @Operation(summary = "도서 단건 상세 조회", description = "ID를 통해 특정 도서의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 도서 ID")
    })
    ResponseEntity<BookDto> getBookById(
            @Parameter(description = "도서 ID (UUID)", required = true) @PathVariable UUID bookId
    );

    @Operation(summary = "이미지 바코드 OCR (ISBN 추출)", description = "업로드된 바코드 이미지에서 ISBN을 추출하여 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추출 성공"),
            @ApiResponse(responseCode = "400", description = "이미지 처리 실패 또는 ISBN 인식 불가"),
            @ApiResponse(responseCode = "500", description = "외부 OCR API 연동 오류")
    })
    ResponseEntity<String> getIsbnByImage(
            @Parameter(description = "바코드 이미지 파일", required = true)
            @RequestPart(value = "image") MultipartFile barcode
    );

    @Operation(summary = "네이버 도서 정보 검색", description = "ISBN을 이용하여 네이버 책 검색 API에서 도서 정보를 가져옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "네이버에서 해당 ISBN의 도서를 찾을 수 없음")
    })
    ResponseEntity<NaverBookDto> getBookInfoByIsbn(
            @Parameter(description = "검색할 ISBN (10자리 또는 13자리)", required = true) @RequestParam String isbn
    );

    @Operation(summary = "도서 생성", description = "새로운 도서를 등록합니다. (썸네일 이미지 포함 가능)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 ISBN")
    })
    ResponseEntity<BookDto> createBook(
            @Parameter(description = "도서 생성 정보 (JSON)", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "bookData") BookCreateRequest createRequest,
            @Parameter(description = "도서 썸네일 이미지 파일")
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnail
    );

    @Operation(summary = "도서 수정", description = "기존 도서 정보를 수정합니다. 썸네일 이미지를 새로 업로드하면 교체됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 도서 ID")
    })
    ResponseEntity<BookDto> updateBook(
            @Parameter(description = "도서 ID", required = true) @PathVariable UUID bookId,
            @Parameter(description = "도서 수정 정보 (JSON)", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "bookData") BookUpdateRequest updateRequest,
            @Parameter(description = "교체할 썸네일 이미지 파일 (선택)")
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnail
    );

    @Operation(summary = "도서 논리 삭제 (Soft Delete)", description = "도서를 논리적으로 삭제 처리합니다 (isDeleted = true).")
    @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)")
    ResponseEntity<Void> deleteBook(
            @Parameter(description = "삭제할 도서 ID", required = true) @PathVariable UUID bookId
    );

    @Operation(summary = "도서 물리 삭제 (Hard Delete)", description = "도서를 데이터베이스에서 완전히 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)")
    ResponseEntity<Void> hardDeleteBook(
            @Parameter(description = "삭제할 도서 ID", required = true) @PathVariable UUID bookId
    );
}
