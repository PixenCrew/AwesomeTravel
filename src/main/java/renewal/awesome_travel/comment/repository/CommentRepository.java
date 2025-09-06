package renewal.awesome_travel.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import renewal.common.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByProductId(Long productId, Pageable pageable);
    Page<Comment> findByWriterId(Long userId, Pageable pageable);

}
