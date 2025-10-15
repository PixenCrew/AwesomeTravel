package renewal.awesome_travel.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.common.entity.Hotel;


public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
