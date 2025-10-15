package renewal.awesome_travel.inquiry.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import renewal.awesome_travel.inquiry.dto.request.InquiryRequestDto;
import renewal.awesome_travel.inquiry.dto.response.InquiryDetailResponseDto;
import renewal.awesome_travel.inquiry.dto.response.InquiryResponseDto;
import renewal.awesome_travel.inquiry.service.InquiryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiry")
public class InquiryUserController {

    private final InquiryService inquiryService;

    // 1:1 문의 작성
    @PostMapping
    public ResponseEntity<Long> createInquiry(@RequestParam Long userId, @RequestBody InquiryRequestDto dto) {
        return ResponseEntity.ok(inquiryService.createInquiry(userId, dto));
    }

    // 내 문의 목록 조회
    @GetMapping
    public ResponseEntity<Page<InquiryResponseDto>> getMyInquiries(
            @RequestParam Long userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(inquiryService.getMyInquiries(userId, pageable));
    }

    // 내 문의 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<InquiryDetailResponseDto> getInquiryDetail(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(inquiryService.getInquiryDetail(id, userId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<InquiryResponseDto>> searchMyInquiries(
            @RequestParam Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isAnswered,
            Pageable pageable
    ) {
        return ResponseEntity.ok(inquiryService.searchMyInquiries(userId, keyword, isAnswered, pageable));
    }

}

