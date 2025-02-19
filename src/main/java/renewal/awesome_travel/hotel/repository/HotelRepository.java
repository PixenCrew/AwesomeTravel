package renewal.awesome_travel.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.hotel.entity.Hotel;


public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
