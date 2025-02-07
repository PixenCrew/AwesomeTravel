package renewal.awesome_travel.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product")
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String depart; //출발지

    @Column(nullable = false)
    private String destination; //도착지

    @Column(nullable = false)
    private Integer duration; //여행기간

    @Column(nullable = false)
    private Integer max_reserve; //최대예약

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description; //설명

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer view; //조회수

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductList> productLists = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "Product_tag",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    public Product(String name, String depart, String destination, Integer duration, Integer max_reserve, String description) {
        this.name = name;
        this.depart = depart;
        this.destination = destination;
        this.duration = duration;
        this.max_reserve = max_reserve;
        this.description = description;
        this.view = 0;
    }

    public void update(String name, String depart, String destination, Integer duration, Integer max_reserve, String description) {
        if (!name.equals(this.name)) this.name = name;
        if (!depart.equals(this.depart)) this.depart = depart;
        if (!destination.equals(this.destination)) this.destination = destination;
        if (!duration.equals(this.duration)) this.duration = duration;
        if (!max_reserve.equals(this.max_reserve)) this.max_reserve = max_reserve;
        if (!description.equals(this.description)) this.description = description;
    }

    public void addProductList(ProductList productList) {
        this.productLists.add(productList);
        productList.setProduct(this);
    }

    public void removeProductList(ProductList productList) {
        this.productLists.remove(productList);
        productList.setProduct(null);
    }

    public void addTag(Tag tag) {
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
            tag.getProducts().add(this); // 양방향 관계 설정
        }
    }

    public void removeTag(Tag tag) {
        if (this.tags.contains(tag)) {
            this.tags.remove(tag);
            tag.getProducts().remove(this); // 연관 관계 삭제
        }
    }

    public void viewCount() {
        this.view++;
    }
}
