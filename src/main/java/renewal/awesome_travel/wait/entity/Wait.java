package renewal.awesome_travel.wait.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.member.entity.User;
import renewal.awesome_travel.product.entity.ProductList;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Waiting_list")
public class Wait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "productList_id")
    private ProductList productList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(nullable = false)
    private LocalDate wait_date;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean purchased;

    public Wait(ProductList productList, User user, LocalDate wait_date) {
        this.productList = productList;
        this.user = user;
        this.wait_date = wait_date;
        this.purchased = false;
    }
}
