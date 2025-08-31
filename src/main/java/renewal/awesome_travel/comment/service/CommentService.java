package renewal.awesome_travel.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import renewal.awesome_travel.comment.dto.request.CommentRequestDto;
import renewal.awesome_travel.comment.dto.response.CommentResponseDto;
import renewal.awesome_travel.comment.entity.Comment;
import renewal.awesome_travel.comment.repository.CommentRepository;
import renewal.awesome_travel.product.entity.Product;
import renewal.awesome_travel.product.repository.ProductRepository;
import renewal.common.entity.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;
    //private final UserRepository userRepository;

    // 댓글 등록
    @Transactional
    public Long createComment(User user, Long productId, CommentRequestDto dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        Comment comment = Comment.create(user, product, dto.getContent(), dto.getRating());
        return commentRepository.save(comment).getId();
    }

    // 댓글 수정 (본인만 가능)
    @Transactional
    public void updateComment(Long commentId, User user, CommentRequestDto dto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getWriter().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인만 수정할 수 있습니다.");
        }

        comment.update(dto.getContent(), dto.getRating());
    }

    // 댓글 삭제 (본인만 가능)
    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getWriter().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }

    // 특정 상품의 댓글 목록
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getCommentsByProduct(Long productId, Pageable pageable) {
        return commentRepository.findByProductId(productId, pageable)
                .map(this::toResponseDto);
    }


    // 내가 쓴 댓글 목록
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getMyComments(User user, Pageable pageable) {
        return commentRepository.findByWriterId(user.getId(), pageable)
                .map(this::toResponseDto);
    }

    private CommentResponseDto toResponseDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .writerName(comment.getWriter().getName())
                .content(comment.getContent())
                .rating(comment.getRating())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}

