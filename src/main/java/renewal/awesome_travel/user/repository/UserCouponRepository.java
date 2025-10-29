package renewal.awesome_travel.user.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import renewal.awesome_travel.user.entity.UserCoupon;
import renewal.common.entity.User;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    // 사용하지 않고 유효기간이 남은 쿠폰 조회
    List<UserCoupon> findByUserAndUsedFalseAndCoupon_ValidUntilAfter(User user, LocalDateTime now);

    // 특정 쿠폰 사용 여부 확인
    List<UserCoupon> findByUserAndCouponId(User user, Long couponId);
}
