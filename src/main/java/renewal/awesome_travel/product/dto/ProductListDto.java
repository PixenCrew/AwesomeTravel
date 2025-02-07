package renewal.awesome_travel.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.product.entity.Product;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductListDto {

    private Long id;

    private Product product;

    private Integer air_id;

    private Integer hotel_id;

    private String date;

    private Integer price;

    private Integer currentReserve;
}
