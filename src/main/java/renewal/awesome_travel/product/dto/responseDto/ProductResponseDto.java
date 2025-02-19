package renewal.awesome_travel.product.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.product.dto.TagDto;
import renewal.awesome_travel.product.dto.requestDto.ProductListRequestDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {

    private Long id;

    private String name;

    private String depart;

    private String destination;

    private Integer duration;

    private Integer max_reserve;

    private String description;

    private Integer view;

    private List<ProductListResponseDto> productLists;

    private List<TagDto> Tags;
}
