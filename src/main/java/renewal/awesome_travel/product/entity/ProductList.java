package renewal.awesome_travel.product.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import renewal.awesome_travel.air.entity.Air;
import renewal.awesome_travel.hotel.entity.Hotel;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_list")
public class ProductList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "productList_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "air_id")
    private Air air;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    // 관계 엔티티 연동
    //    @OneToMany(mappedBy = "pack", cascade = CascadeType.ALL)
    //    private List<PackageHotel> packageHotels = new ArrayList<>();

    private String date; //출발일

    private Integer price;

    private Integer currentReserve; //현재 예약된 인원

    public ProductList(Product product, Air air, Hotel hotel, String date, Integer price, Integer currentReserve) {
        this.product = product;
        this.air = air;
        this.hotel = hotel;
        this.date = date;
        this.price = price;
        this.currentReserve = currentReserve;
    }

    public void updateProductList(Air air, Hotel hotel, String date, Integer price, Integer currentReserve) {
        if (!air.equals(this.air)) this.air = air;
        if (!hotel.equals(this.hotel)) this.hotel = hotel;
        if (!date.equals(this.date)) this.date = date;
        if (!price.equals(this.price)) this.price = price;
        if (!currentReserve.equals(this.currentReserve)) this.currentReserve = currentReserve;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
