package renewal.awesome_travel.inquiry.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.inquiry.repository.InquiryAnswerRepository;
import renewal.awesome_travel.inquiry.repository.InquiryRepository;
import renewal.awesome_travel.inquiry.service.InquiryService;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.common.entity.Inquiry;
import renewal.common.entity.InquiryAnswer;
import renewal.common.entity.User;
import renewal.common.entity.User.UserRole;

@RequiredArgsConstructor
@Controller
@RequestMapping("/inquiry")
public class InquiryController {

    private final InquiryRepository inquiryRepo;
    private final InquiryAnswerRepository answerRepo;
    private final InquiryService inquiryService;
    private final UserRepository userRepo;

    // 내 문의 상세 조회
    @GetMapping("{id}")
    public String getInquiryDetail(@PathVariable Long id, Model model, Principal principal) {

        Inquiry inquiry = inquiryRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문의가 존재하지 않습니다."));

        if (inquiry.isPrivate()) {
            Optional<User> currentUser = Optional.empty();
            if (principal != null) {
                currentUser = userRepo.findByEmail(principal.getName());
            }

            boolean isOwner = currentUser
                    .map(User::getId)
                    .map(idVal -> idVal.equals(inquiry.getUser().getId()))
                    .orElse(false);

            boolean isAdmin = currentUser
                    .map(User::getRole)
                    .filter(role -> role == UserRole.ADMIN)
                    .isPresent();

            if (!isOwner && !isAdmin) {
                return "error/error";
            }
        }

        model.addAttribute("inquiry", inquiry);

        InquiryAnswer answer = answerRepo.findByInquiryId(id).orElse(null);
        model.addAttribute("answer", answer); // answer가 null일 수도 있음

        return "fragments/inquiry/inquiryDetail";
    }

    // 문의 등록 폼
    @GetMapping("/new")
    public String getInquiryForm(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long purchaseId,
            Model model) {
        if (productId != null) {
            model.addAttribute("productId", productId);
            model.addAttribute("stage", "BEFORE_PURCHASE");
        } else if (purchaseId != null) {
            model.addAttribute("purchaseId", purchaseId);
            model.addAttribute("stage", "AFTER_BOOKING");
        } else {
            // productId와 purchaseId가 모두 없으면 일반 문의
            model.addAttribute("stage", "GENERAL");
        }
        return "fragments/inquiry/inquiryForm";
    }

    // 모든 문의 목록 조회 (고객문의)
    @GetMapping("/all")
    public String getAllInquiries(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        Page<renewal.awesome_travel.inquiry.dto.response.InquiryResponseDto> inquiries = inquiryService.getAllInquiries(pageable);
        model.addAttribute("inquiries", inquiries);
        return "fragments/inquiry/inquiryAllList";
    }

}
