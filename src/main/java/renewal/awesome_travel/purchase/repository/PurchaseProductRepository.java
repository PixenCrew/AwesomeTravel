package renewal.awesome_travel.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import renewal.common.entity.PurchaseProduct;

import java.util.Optional;

public interface PurchaseProductRepository extends JpaRepository<PurchaseProduct, Long> {

    @Query("SELECT DISTINCT pp FROM PurchaseProduct pp " +
            "JOIN FETCH pp.product " +
            "LEFT JOIN FETCH pp.productPassengers p " +
            "LEFT JOIN FETCH p.nationality " +
            "WHERE pp.id = :id")
    Optional<PurchaseProduct> findByIdWithAll(@Param("id") Long id);
}
