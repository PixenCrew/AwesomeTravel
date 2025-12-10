package renewal.awesome_travel.review.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.awesome_travel.review.dto.request.ReviewRequestDto;
import renewal.awesome_travel.review.dto.response.ReviewResponseDto;
import renewal.awesome_travel.review.service.ReviewService;
import renewal.common.entity.User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ReviewController {

    private final ReviewService commentService;

    // 댓글 등록
    @PostMapping("/{productId}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ReviewRequestDto dto) {
        try {
            User user = userDetails.getUser();
            Long commentId = commentService.createComment(user, productId, dto);
            return ResponseEntity.ok(commentId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }

    // 댓글 수정
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ReviewRequestDto dto) {
        try {
            User user = userDetails.getUser();
            commentService.updateComment(commentId, user, dto);
            return ResponseEntity.ok(java.util.Map.of("message", "리뷰가 수정되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        commentService.deleteComment(commentId, user);
        return ResponseEntity.ok().build();
    }

    // 상품별 댓글 목록
    @GetMapping("/{productId}/comments")
    public ResponseEntity<Page<ReviewResponseDto>> getCommentsByProduct(
            @PathVariable Long productId,
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getCommentsByProduct(productId, pageable));
    }

    // 내가 쓴 댓글 목록
    @GetMapping("/comments/my")
    public ResponseEntity<Page<ReviewResponseDto>> getMyComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(commentService.getMyComments(user, pageable));
    }

}
