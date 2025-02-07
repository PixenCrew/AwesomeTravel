package renewal.awesome_travel.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.product.entity.ProductList;
import renewal.awesome_travel.product.entity.Tag;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {

    private Long id;

    private String name;

    private String depart;

    private String destination;

    private Integer duration;

    private Integer max_reserve;

    private String description;

    private Integer view;

    private List<ProductList> productLists;

    private List<Tag> Tags;
}
