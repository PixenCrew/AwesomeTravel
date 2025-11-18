package renewal.awesome_travel.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.review.dto.request.ReviewRequestDto;
import renewal.awesome_travel.review.dto.response.ReviewResponseDto;
import renewal.awesome_travel.review.repository.ReviewRepository;
import renewal.common.entity.Product;
import renewal.common.entity.Review;
import renewal.common.entity.User;
import renewal.common.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository commentRepository;
    private final ProductRepository productRepository;
    // private final UserRepository userRepository;

    // 댓글 등록
    @Transactional
    public Long createComment(User user, Long productId, ReviewRequestDto dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        Review comment = Review.create(user, product, dto.getContent(), dto.getRating());
        return commentRepository.save(comment).getId();
    }

    // 댓글 수정 (본인만 가능)
    @Transactional
    public void updateComment(Long commentId, User user, ReviewRequestDto dto) {
        Review comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getWriter().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인만 수정할 수 있습니다.");
        }

        comment.update(dto.getContent(), dto.getRating());
    }

    // 댓글 삭제 (본인만 가능)
    @Transactional
    public void deleteComment(Long commentId, User user) {
        Review comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getWriter().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }

    // 특정 상품의 댓글 목록
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getCommentsByProduct(Long productId, Pageable pageable) {
        return commentRepository.findByProductId(productId, pageable)
                .map(this::toResponseDto);
    }

    // 내가 쓴 댓글 목록
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getMyComments(User user, Pageable pageable) {
        return commentRepository.findByWriterId(user.getId(), pageable)
                .map(this::toResponseDto);
    }

    private ReviewResponseDto toResponseDto(Review comment) {
        return ReviewResponseDto.builder()
                .id(comment.getId())
                .writerName(comment.getWriter().getName())
                .content(comment.getContent())
                .rating(comment.getRating())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
