package renewal.awesome_travel.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.member.entity.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMemberId(Long memberId);
}
