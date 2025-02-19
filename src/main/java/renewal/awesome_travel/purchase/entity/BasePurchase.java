package renewal.awesome_travel.purchase.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.purchase.utiles.Status;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@MappedSuperclass
public abstract class BasePurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_purchase_id")
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private Integer price; //결제금액

    @Column(nullable = false)
    private Long member_id; //구매자

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime purchaseDate; //구매일

    @Column
    private LocalDateTime paymentDueDate; //결제기한

    public BasePurchase(Status status, Integer price, Long member_id, String name, String number, String email, LocalDateTime purchaseDate, LocalDateTime paymentDueDate) {
        this.status = status;
        this.price = price;
        this.member_id = member_id;
        this.name = name;
        this.number = number;
        this.email = email;
        this.purchaseDate = purchaseDate;
        this.paymentDueDate = paymentDueDate;
    }
}
