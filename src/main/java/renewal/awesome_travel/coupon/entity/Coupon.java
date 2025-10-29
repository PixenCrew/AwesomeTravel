package renewal.awesome_travel.coupon.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.coupon.utiles.Coupon_type;
import renewal.awesome_travel.coupon.utiles.Grade;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer coupon_id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Coupon_type couponType;

    @Column(nullable = false)
    private Integer value;

    @Column(nullable = false)
    private Integer max_discount; // 최대 할인금액

    @Column(nullable = false)
    private Integer min_price; // 최소 주문금액

    @Column(nullable = false)
    private Grade target;

    @Column(nullable = false)
    private Integer current;

    @Column(nullable = false)
    private LocalDateTime issue_start;

    @Column(nullable = false)
    private LocalDateTime issue_end;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    public Coupon(Integer coupon_id, String name, String description, Coupon_type couponType, Integer value,
            Integer max_discount, Integer min_price, Grade target, Integer current, LocalDateTime issue_start,
            LocalDateTime issue_end, LocalDateTime validFrom, LocalDateTime validUntil) {
        this.coupon_id = coupon_id;
        this.name = name;
        this.description = description;
        this.couponType = couponType;
        this.value = value;
        this.max_discount = max_discount;
        this.min_price = min_price;
        this.target = target;
        this.current = current;
        this.issue_start = issue_start;
        this.issue_end = issue_end;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }
}
