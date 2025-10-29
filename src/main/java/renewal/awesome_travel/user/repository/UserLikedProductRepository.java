package renewal.awesome_travel.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import renewal.awesome_travel.user.entity.UserLikedProduct;
import renewal.common.entity.User;

@Repository
public interface UserLikedProductRepository extends JpaRepository<UserLikedProduct, Long> {

    // 특정 사용자 활성화된 찜 상품 조회
    List<UserLikedProduct> findByUserAndActiveTrueOrderByLikedAtDesc(User user);

    // 특정 상품 찜 여부 확인
    Optional<UserLikedProduct> findByUserAndProductId(User user, Long productId);
}
