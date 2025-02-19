package renewal.awesome_travel.purchase.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.product.entity.Product;
import renewal.awesome_travel.purchase.utiles.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "ProductPurchase")
public class ProductPurchase extends BasePurchase {

    @ManyToOne
    @JoinColumn(name = "purchase_target_id", nullable = false)
    private Product product; //구매상품

    @OneToMany(mappedBy = "productPurchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductPassenger> productPassengers = new ArrayList<>();

    public ProductPurchase(Product product, Status status, Integer price, Long member_id, String name, String number, String email, LocalDateTime purchaseDate, LocalDateTime paymentDueDate) {
        super(status, price, member_id, name, number, email, purchaseDate, paymentDueDate);
        this.product = product;
    }
}
