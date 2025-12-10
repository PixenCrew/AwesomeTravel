package renewal.awesome_travel.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.review.dto.request.ReviewRequestDto;
import renewal.awesome_travel.review.dto.response.ReviewResponseDto;
import renewal.awesome_travel.review.repository.ReviewRepository;
import renewal.awesome_travel.review.util.ProfanityFilter;
import renewal.common.entity.Product;
import renewal.common.entity.Review;
import renewal.common.entity.User;
import renewal.common.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository commentRepository;
    private final ProductRepository productRepository;
    private final ProfanityFilter profanityFilter;
    // private final UserRepository userRepository;

    // 댓글 등록
    @Transactional
    public Long createComment(User user, Long productId, ReviewRequestDto dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        // 비속어 필터링
        if (profanityFilter.containsProfanity(dto.getContent())) {
            throw new IllegalArgumentException("부적절한 단어가 포함되어 있습니다. 리뷰를 작성할 수 없습니다.");
        }

        Review comment = Review.create(user, product, dto.getContent(), dto.getRating());
        commentRepository.save(comment);
        
        // Product의 리뷰 통계 업데이트
        product.UpdateAvg(dto.getRating());
        productRepository.save(product);
        
        return comment.getId();
    }

    // 댓글 수정 (본인만 가능)
    @Transactional
    public void updateComment(Long commentId, User user, ReviewRequestDto dto) {
        Review comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getWriter().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인만 수정할 수 있습니다.");
        }

        // 비속어 필터링
        if (profanityFilter.containsProfanity(dto.getContent())) {
            throw new IllegalArgumentException("부적절한 단어가 포함되어 있습니다. 리뷰를 수정할 수 없습니다.");
        }

        // 이전 rating 감소
        Product product = comment.getProduct();
        int oldRating = comment.getRating();
        decreaseStar(product, oldRating);
        
        // 새로운 rating 증가
        comment.update(dto.getContent(), dto.getRating());
        product.UpdateAvg(dto.getRating());
        productRepository.save(product);
    }

    // 댓글 삭제 (본인만 가능)
    @Transactional
    public void deleteComment(Long commentId, User user) {
        Review comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getWriter().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인만 삭제할 수 있습니다.");
        }

        // Product의 리뷰 통계에서 rating 감소
        Product product = comment.getProduct();
        decreaseStar(product, comment.getRating());
        productRepository.save(product);
        
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
                .map(review -> {
                    // Product 정보 초기화
                    if (review.getProduct() != null) {
                        org.hibernate.Hibernate.initialize(review.getProduct());
                    }
                    return toResponseDto(review);
                });
    }

    private ReviewResponseDto toResponseDto(Review comment) {
        return ReviewResponseDto.builder()
                .id(comment.getId())
                .productId(comment.getProduct() != null ? comment.getProduct().getId() : null)
                .productTitle(comment.getProduct() != null ? comment.getProduct().getTitle() : null)
                .writerName(comment.getWriter().getName())
                .content(comment.getContent())
                .rating(comment.getRating())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
    
    private void decreaseStar(Product product, int rating) {
        switch (rating) {
            case 1:
                if (product.getStar1() > 0) product.setStar1(product.getStar1() - 1);
                break;
            case 2:
                if (product.getStar2() > 0) product.setStar2(product.getStar2() - 1);
                break;
            case 3:
                if (product.getStar3() > 0) product.setStar3(product.getStar3() - 1);
                break;
            case 4:
                if (product.getStar4() > 0) product.setStar4(product.getStar4() - 1);
                break;
            case 5:
                if (product.getStar5() > 0) product.setStar5(product.getStar5() - 1);
                break;
        }
    }
}
