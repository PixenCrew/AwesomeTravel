package renewal.awesome_travel.product.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.product.dto.TagDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDto {

    private String name;

    private String depart;

    private String destination;

    private Integer duration;

    private Integer max_reserve;

    private String description;

    private List<ProductListRequestDto> productLists;

    private List<Long> tagIds;
}
