package renewal.awesome_travel.purchase.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.air.entity.SeatClass;
import renewal.awesome_travel.purchase.utiles.PurchaseStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "AirPurchase")
public class AirPurchase extends BasePurchase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_class_id", nullable = false)
    private SeatClass seatClass;

    @OneToMany(mappedBy = "airPurchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AirPassenger> airPassengers = new ArrayList<>();

    public AirPurchase(SeatClass seatClass, Integer price, Long member_id, String name, String number, String email, LocalDateTime purchaseDate, LocalDateTime paymentDueDate) {
        super(price, member_id, name, number, email, purchaseDate, paymentDueDate);
        this.seatClass = seatClass;

    }

    public void setPurchaseStatus(PurchaseStatus newStatus) {
        // 상태가 이미 PAID인데 다시 HOLDING으로 바꾸려는 경우 방지
        if (this.purchaseStatus == PurchaseStatus.PAID && newStatus == PurchaseStatus.HOLDING) {
            throw new IllegalStateException("결제 완료된 구매는 HOLDING 상태로 되돌릴 수 없습니다.");
        }
        if (this.purchaseStatus == PurchaseStatus.CANCELLED && newStatus != PurchaseStatus.CANCELLED) {
            throw new IllegalStateException("취소된 구매는 상태를 변경할 수 없습니다.");
        }

        this.purchaseStatus = newStatus;
    }
}
