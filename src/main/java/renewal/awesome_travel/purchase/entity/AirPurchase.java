package renewal.awesome_travel.purchase.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.air.entity.Air;
import renewal.awesome_travel.purchase.utiles.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "AirPurchase")
public class AirPurchase extends BasePurchase {

    @ManyToOne
    @JoinColumn(name = "purchase_target_id", nullable = false)
    private Air air; //구매항공

    @OneToMany(mappedBy = "airPurchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AirPassenger> airPassengers = new ArrayList<>();

    public AirPurchase(Air air, Status status, Integer price, Long member_id, String name, String number, String email, LocalDateTime purchaseDate, LocalDateTime paymentDueDate) {
        super(status,price, member_id, name, number, email, purchaseDate, paymentDueDate);
        this.air = air;

    }
}
