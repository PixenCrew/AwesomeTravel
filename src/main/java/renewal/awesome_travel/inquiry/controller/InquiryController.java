package renewal.awesome_travel.inquiry.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.inquiry.repository.InquiryAnswerRepository;
import renewal.awesome_travel.inquiry.repository.InquiryRepository;
import renewal.common.entity.Inquiry;
import renewal.common.entity.InquiryAnswer;

@RequiredArgsConstructor
@Controller
@RequestMapping("/inquiry")
public class InquiryController {

    private final InquiryRepository inquiryRepo;
    private final InquiryAnswerRepository answerRepo;

    // 내 문의 상세 조회
    @GetMapping("{id}")
    public String getInquiryDetail(@PathVariable Long id, Model model) {

        Inquiry inquiry = inquiryRepo.findById(id).get();
        model.addAttribute("inquiry", inquiry);

        InquiryAnswer answer = answerRepo.findByInquiryId(id).orElse(null);
        model.addAttribute("answer", answer); // answer가 null일 수도 있음

        return "fragments/inquiry/inquiryDetail";
    }

}
