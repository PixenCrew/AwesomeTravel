package renewal.awesome_travel.purchase.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import renewal.common.entity.PurchaseBase.PurchaseStatus;
import renewal.common.entity.PurchaseAir;

import java.time.LocalDateTime;

public interface PurchaseAirRepository extends JpaRepository<PurchaseAir, Long> {

    Page<PurchaseAir> findByPurchaseStatusAndPaymentDueDateBefore(
            PurchaseStatus status,
            LocalDateTime time,
            Pageable pageable
    );


}
