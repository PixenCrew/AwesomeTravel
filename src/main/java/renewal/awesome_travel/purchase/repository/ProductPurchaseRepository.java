package renewal.awesome_travel.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import renewal.common.entity.ProductPurchase;

import java.util.Optional;

public interface ProductPurchaseRepository extends JpaRepository<ProductPurchase, Long> {

    @Query("SELECT DISTINCT pp FROM ProductPurchase pp " +
            "JOIN FETCH pp.product " +
            "LEFT JOIN FETCH pp.productPassengers p " +
            "LEFT JOIN FETCH p.nationality " +
            "WHERE pp.id = :id")
    Optional<ProductPurchase> findByIdWithAll(@Param("id") Long id);
}
