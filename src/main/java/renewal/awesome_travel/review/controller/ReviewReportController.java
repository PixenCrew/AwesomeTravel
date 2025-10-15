package renewal.awesome_travel.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.awesome_travel.review.dto.request.ReviewReportRequestDto;
import renewal.awesome_travel.review.service.ReviewReportService;
import renewal.common.entity.User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/reviews")
public class ReviewReportController {

    private final ReviewReportService reviewReportService;

    // 댓글 신고
    @PostMapping("/{commentId}/report")
    public ResponseEntity<Void> reportComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ReviewReportRequestDto dto
    ) {
        User user = userDetails.getUser();
        reviewReportService.reportReview(commentId, user, dto);
        return ResponseEntity.ok().build();
    }
}

