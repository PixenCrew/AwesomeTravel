package renewal.awesome_travel.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.purchase.entity.SpecialRequest;

public interface SpecialRequestRepository extends JpaRepository<SpecialRequest, Long> {
}
