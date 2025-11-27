package renewal.awesome_travel.review.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import renewal.common.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductId(Long productId, Pageable pageable);

    Page<Review> findByWriterId(Long userId, Pageable pageable);

    @Query("SELECT r.rating AS rating, COUNT(r) AS count FROM Review r WHERE r.product.id = :productId GROUP BY r.rating")
    List<RatingCount> countByProductIdGroupByRating(@Param("productId") Long productId);

    interface RatingCount {
        Integer getRating();

        Long getCount();
    }

}
