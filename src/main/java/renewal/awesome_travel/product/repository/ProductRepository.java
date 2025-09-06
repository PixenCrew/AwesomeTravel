package renewal.awesome_travel.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.common.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
