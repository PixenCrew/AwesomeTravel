package renewal.awesome_travel.purchase.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import renewal.common.entity.Product;
import renewal.common.entity.PurchaseProduct;

public interface PurchaseProductRepository extends JpaRepository<PurchaseProduct, Long> {

        @Query("SELECT DISTINCT pp FROM PurchaseProduct pp " +
                        "JOIN FETCH pp.product " +
                        "LEFT JOIN FETCH pp.passengers p " +
                        "LEFT JOIN FETCH p.nationality " +
                        "WHERE pp.id = :id")
        Optional<PurchaseProduct> findByIdWithAll(@Param("id") Long id);

        List<PurchaseProduct> findByUserId(Long userid);

        @Query("""
                         SELECT p
                         FROM PurchaseProduct p
                         WHERE p.product = :product
                           AND DATE(p.departDateTime) = :departDate
                         ORDER BY p.purchaseDate ASC
                        """)
        List<PurchaseProduct> findByProductAndDepartDate(
                        @Param("product") Product product,
                        @Param("departDate") LocalDate departDate);

}
