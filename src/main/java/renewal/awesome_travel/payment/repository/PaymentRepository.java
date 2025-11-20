package renewal.awesome_travel.payment.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import renewal.common.entity.Payment;
import renewal.common.entity.User;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    int countByUserAndPurchaseStatusAndPurchaseDateBetween(
            User user,
            Payment.PaymentStatus status,
            LocalDateTime from,
            LocalDateTime to);

    List<Payment> findByUserAndPurchaseStatusAndPurchaseDateBetween(
            User user,
            Payment.PaymentStatus status,
            LocalDateTime from,
            LocalDateTime to);
}
