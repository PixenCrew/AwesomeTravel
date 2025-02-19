package renewal.awesome_travel.purchase.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.purchase.utiles.Sex;

import java.time.LocalDate;
import java.util.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "ProductPassenger")
public class ProductPassenger extends BasePassenger{

    @ManyToOne
    @JoinColumn(name = "product_purchase_id", nullable = false)
    private ProductPurchase productPurchase;

    //요구사항(객실, 투어옵션, 여행자보험, 룸메이트 등등) 추후 enum이나 엔티티로 변경
    @ManyToMany
    @JoinTable(
            name = "product_passenger_special_requests",
            joinColumns = @JoinColumn(name = "product_passenger_id"),
            inverseJoinColumns = @JoinColumn(name = "special_request_id")
    )
    private Set<SpecialRequest> specialRequests = new HashSet<>(); //요구사항

    public ProductPassenger(ProductPurchase productPurchase, String name, String number, String email, LocalDate birth, Sex sex, Country nationality, String passport_num, String lastName, String firstName, LocalDate expire) {
        super(name, number, email, birth, sex, nationality, passport_num, lastName, firstName, expire);
        this.productPurchase = productPurchase;
    }

    // SpecialRequest 설정
    public void addSpecialRequests(Set<SpecialRequest> specialRequests) {
        if (specialRequests != null) {
            this.specialRequests.addAll(specialRequests);
        }
    }
}
