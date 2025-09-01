package renewal.awesome_travel.purchase.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import renewal.common.entity.BasePurchase.PurchaseStatus;
import renewal.common.entity.AirPurchase;

import java.time.LocalDateTime;

public interface AirPurchaseRepository extends JpaRepository<AirPurchase, Long> {

    Page<AirPurchase> findByPurchaseStatusAndPaymentDueDateBefore(
            PurchaseStatus status,
            LocalDateTime time,
            Pageable pageable
    );


}
