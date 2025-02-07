package renewal.awesome_travel.product.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

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

    @Column(nullable = false)
    private Integer air_id;

    @Column(nullable = false)
    private Integer hotel_id;

    private String date; //출발일

    private Integer price;

    private Integer currentReserve; //현재 예약된 인원

    public ProductList(Product product, Integer air_id, Integer hotel_id, String date, Integer price, Integer currentReserve) {
        this.product = product;
        this.air_id = air_id;
        this.hotel_id = hotel_id;
        this.date = date;
        this.price = price;
        this.currentReserve = currentReserve;
    }

    public void updateProductList(Integer air_id, Integer hotel_id, String date, Integer price, Integer currentReserve) {
        if (!air_id.equals(this.air_id)) this.air_id = air_id;
        if (!hotel_id.equals(this.hotel_id)) this.hotel_id = hotel_id;
        if (!date.equals(this.date)) this.date = date;
        if (!price.equals(this.price)) this.price = price;
        if (!currentReserve.equals(this.currentReserve)) this.currentReserve = currentReserve;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
