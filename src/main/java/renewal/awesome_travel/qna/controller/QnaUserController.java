package renewal.awesome_travel.qna.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import renewal.awesome_travel.qna.dto.request.QnaAnswerRequestDto;
import renewal.awesome_travel.qna.dto.request.QnaAnswerUpdateRequestDto;
import renewal.awesome_travel.qna.dto.request.QnaRequestDto;
import renewal.awesome_travel.qna.dto.request.QnaUpdateRequestDto;
import renewal.awesome_travel.qna.dto.response.QnaDetailResponseDto;
import renewal.awesome_travel.qna.dto.response.QnaResponseDto;
import renewal.awesome_travel.qna.service.QnaService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qna")
public class QnaUserController {

    private final QnaService qnaService;

    @PostMapping
    public ResponseEntity<Long> createQna(@RequestParam Long writerId, @RequestBody QnaRequestDto dto) {
        return ResponseEntity.ok(qnaService.createQna(writerId, dto));
    }

    @GetMapping
    public ResponseEntity<Page<QnaResponseDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(qnaService.getAllQna(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QnaDetailResponseDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(qnaService.getQna(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<QnaResponseDto>> searchQna(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(qnaService.searchQna(keyword, pageable));
    }


    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateQna(@PathVariable Long id, @RequestParam Long requestUserId, @RequestBody QnaUpdateRequestDto dto) {
        qnaService.updateQnaPartial(id, requestUserId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQna(@PathVariable Long id, @RequestParam Long requestUserId) {
        qnaService.deleteQna(id, requestUserId);
        return ResponseEntity.ok().build();
    }
}
