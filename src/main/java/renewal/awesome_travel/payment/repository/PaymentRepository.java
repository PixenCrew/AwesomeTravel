package renewal.awesome_travel.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import renewal.common.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
