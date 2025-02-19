package renewal.awesome_travel.air.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.air.entity.Air;

public interface AirRepository extends JpaRepository<Air, Long> {
}
