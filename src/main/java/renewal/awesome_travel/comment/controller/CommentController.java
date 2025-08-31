package renewal.awesome_travel.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import renewal.awesome_travel.comment.dto.request.CommentRequestDto;
import renewal.awesome_travel.comment.dto.response.CommentResponseDto;
import renewal.awesome_travel.comment.service.CommentService;
import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.common.entity.User;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class CommentController {

    private final CommentService commentService;

    // 댓글 등록
    @PostMapping("/{productId}/comments")
    public ResponseEntity<Long> createComment(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentRequestDto dto
    ) {
        User user = userDetails.getUser();
        Long commentId = commentService.createComment(user, productId, dto);
        return ResponseEntity.ok(commentId);
    }

    // 댓글 수정
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentRequestDto dto
    ) {
        User user = userDetails.getUser();
        commentService.updateComment(commentId, user, dto);
        return ResponseEntity.ok().build();
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        commentService.deleteComment(commentId, user);
        return ResponseEntity.ok().build();
    }

    // 상품별 댓글 목록
    @GetMapping("/{productId}/comments")
    public ResponseEntity<Page<CommentResponseDto>> getCommentsByProduct(
            @PathVariable Long productId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(commentService.getCommentsByProduct(productId, pageable));
    }

    // 내가 쓴 댓글 목록
    @GetMapping("/comments/my")
    public ResponseEntity<Page<CommentResponseDto>> getMyComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable
    ) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(commentService.getMyComments(user, pageable));
    }

}

