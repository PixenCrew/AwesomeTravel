package renewal.awesome_travel.purchase.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.purchase.entity.AirPurchase;
import renewal.awesome_travel.purchase.utiles.PurchaseStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AirPurchaseRepository extends JpaRepository<AirPurchase, Long> {

    Page<AirPurchase> findByPurchaseStatusAndPaymentDueDateBefore(
            PurchaseStatus status,
            LocalDateTime time,
            Pageable pageable
    );


}
