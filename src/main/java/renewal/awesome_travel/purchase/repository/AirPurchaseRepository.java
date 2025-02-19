package renewal.awesome_travel.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import renewal.awesome_travel.purchase.entity.AirPurchase;

import java.util.Optional;

public interface AirPurchaseRepository extends JpaRepository<AirPurchase, Long> {

    // 🔹 JOIN FETCH 사용하여 Air를 미리 가져옴
    @Query("SELECT ap FROM AirPurchase ap JOIN FETCH ap.air WHERE ap.id = :id")
    Optional<AirPurchase> findByIdWithAir(@Param("id") Long id);

    @Query("select distinct ap from AirPurchase ap join fetch ap.air " +
            "LEFT join fetch ap.airPassengers p " +
            "left join fetch p.nationality where ap.id = :id")
    Optional<AirPurchase> findByIdWithAll(@Param("id") Long id);
}
