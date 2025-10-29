package renewal.awesome_travel.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import renewal.awesome_travel.user.entity.UserRecentProduct;
import renewal.common.entity.User;

@Repository
public interface UserRecentProductRepository extends JpaRepository<UserRecentProduct, Long> {

    // 특정 사용자 최근 본 상품 Top N 조회 (최근 본 순서)
    List<UserRecentProduct> findTop20ByUserOrderByViewedAtDesc(User user);

    // 특정 상품 최근 본 기록 조회 (optional)
    List<UserRecentProduct> findByUserAndProductId(User user, Long productId);
}
