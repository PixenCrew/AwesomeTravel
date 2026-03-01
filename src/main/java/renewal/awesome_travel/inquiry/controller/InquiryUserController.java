package renewal.awesome_travel.inquiry.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.awesome_travel.inquiry.dto.request.InquiryRequestDto;
import renewal.awesome_travel.inquiry.dto.response.InquiryDetailResponseDto;
import renewal.awesome_travel.inquiry.dto.response.InquiryResponseDto;
import renewal.awesome_travel.inquiry.service.InquiryService;
import renewal.common.entity.User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiry")
public class InquiryUserController {

    private final InquiryService inquiryService;

    // 1:1 문의 작성
    @PostMapping
    public ResponseEntity<Long> createInquiry(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody InquiryRequestDto dto) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userDetails.getUser();
        return ResponseEntity.ok(inquiryService.createInquiry(user.getId(), dto));
    }

    // 내 문의 목록 조회 (현재 로그인 사용자)
    @GetMapping
    public ResponseEntity<Page<InquiryResponseDto>> getMyInquiries(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(inquiryService.getMyInquiries(userDetails.getUser().getId(), pageable));
    }

    // 내 문의 상세 조회 (현재 로그인 사용자)
    @GetMapping("/{id}")
    public ResponseEntity<InquiryDetailResponseDto> getInquiryDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(inquiryService.getInquiryDetail(id, userDetails.getUser().getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<InquiryResponseDto>> searchMyInquiries(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isAnswered,
            Pageable pageable
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(inquiryService.searchMyInquiries(
                userDetails.getUser().getId(), keyword, isAnswered, pageable));
    }

}

