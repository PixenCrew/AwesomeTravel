package renewal.awesome_travel.faq.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import renewal.awesome_travel.faq.dto.response.FaqResponseDto;
import renewal.awesome_travel.faq.service.FaqService;
import renewal.awesome_travel.faq.utils.FaqCategory;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/faqs")
public class FaqController {

    private final FaqService faqService;

    // 전체 FAQ 조회
    @GetMapping
    public ResponseEntity<Page<FaqResponseDto>> getAllFaqs(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(faqService.getAllFaqs(pageable));
    }

    // 카테고리별 FAQ 조회
    @GetMapping("/category")
    public ResponseEntity<Page<FaqResponseDto>> getFaqsByCategory(
            @RequestParam FaqCategory category,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(faqService.getFaqsByCategory(category, pageable));
    }
}


