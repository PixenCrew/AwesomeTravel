package renewal.awesome_travel.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import renewal.common.entity.SpecialRequest;

public interface SpecialRequestRepository extends JpaRepository<SpecialRequest, Long> {
}
