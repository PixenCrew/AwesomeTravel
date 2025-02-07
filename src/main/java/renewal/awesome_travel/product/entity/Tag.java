package renewal.awesome_travel.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String tag;

    @ManyToMany(mappedBy = "tags")
    private List<Product> products = new ArrayList<>();

    public Tag(String tag) {
        this.tag = tag;
    }


}
