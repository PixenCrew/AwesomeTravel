package renewal.awesome_travel.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchRequestDto {

    private String keyword;

    private String menuCode;

    private int page;

    // private String destination;

    // private Integer duration;

    // private Integer max_reserve;

    // private String description;

    // // private List<ProductListRequestDto> productLists;

    // private List<Long> tagIds;
}
