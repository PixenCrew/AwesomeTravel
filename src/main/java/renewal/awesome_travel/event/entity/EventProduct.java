package renewal.awesome_travel.event.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.product.entity.Product;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "EventProduct")
public class EventProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EventProduct_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer price; // 변경된 가격

    public EventProduct(Event event, Product product, Integer price) {
        this.event = event;
        this.product = product;
        this.price = price;
    }

    public void setEvent(Event event) {
        this.event = event;
    }


}
