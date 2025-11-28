package renewal.awesome_travel.faq.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import renewal.common.entity.Faq;
import renewal.common.entity.Faq.FaqCategory;
import renewal.awesome_travel.faq.repositiry.FaqRepository;

@Controller
@RequiredArgsConstructor
@RequestMapping("/faq")
public class FaqViewController {

    private final FaqRepository faqRepository;

    @GetMapping
    public String getFaqList(
            @RequestParam(required = false) FaqCategory category,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        
        Page<Faq> faqs;
        
        if (category != null) {
            faqs = faqRepository.findByCategoryAndVisibleTrue(category, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            faqs = faqRepository.findByKeywordAndVisibleTrue(keyword.trim(), pageable);
        } else {
            faqs = faqRepository.findByVisibleTrue(pageable);
        }
        
        model.addAttribute("faqs", faqs.getContent());
        model.addAttribute("totalCount", faqs.getTotalElements());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", FaqCategory.values());
        
        return "fragments/faq/faqList";
    }
}

