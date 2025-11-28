package renewal.awesome_travel.notice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import renewal.awesome_travel.notice.repository.NoticeRepository;
import renewal.common.entity.Notice;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeRepository noticeRepository;

    @GetMapping
    public String getNoticeList(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        
        Page<Notice> notices = noticeRepository.findByIsVisibleTrueOrderByFixDescCreatedAtDesc(pageable);
        model.addAttribute("notices", notices.getContent());
        
        return "fragments/notice/noticeList";
    }

    @GetMapping("/{id}")
    public String getNoticeDetail(@PathVariable Long id, Model model) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
        
        model.addAttribute("notice", notice);
        
        return "fragments/notice/noticeDetail";
    }
}


