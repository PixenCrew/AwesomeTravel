package renewal.awesome_travel.purchase.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import renewal.common.entity.PurchaseAir;
import renewal.common.entity.PurchaseBase.PurchaseStatus;

public interface PurchaseAirRepository extends JpaRepository<PurchaseAir, Long> {
    List<PurchaseAir> findByUserId(Long userid);

    Page<PurchaseAir> findByPurchaseStatusAndPaymentDueDateBefore(
            PurchaseStatus status,
            LocalDateTime time,
            Pageable pageable);

}
