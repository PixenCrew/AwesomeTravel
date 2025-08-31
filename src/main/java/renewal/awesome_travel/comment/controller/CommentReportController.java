package renewal.awesome_travel.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import renewal.awesome_travel.comment.dto.request.CommentReportRequestDto;
import renewal.awesome_travel.comment.service.CommentReportService;
import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.common.entity.User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/comments")
public class CommentReportController {

    private final CommentReportService commentReportService;

    // 댓글 신고
    @PostMapping("/{commentId}/report")
    public ResponseEntity<Void> reportComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentReportRequestDto dto
    ) {
        User user = userDetails.getUser();
        commentReportService.reportComment(commentId, user, dto);
        return ResponseEntity.ok().build();
    }
}

