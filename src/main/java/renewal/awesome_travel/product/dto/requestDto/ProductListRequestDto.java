package renewal.awesome_travel.product.dto.requestDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductListRequestDto {

    private Long product_id;

    private Long air_id;

    private Long hotel_id;

    private String date;

    private Integer price;

    private Integer currentReserve;
}
