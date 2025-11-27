package renewal.awesome_travel.product.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductCompareViewDto {

    private final Long productId;
    private final String title;
    private final String priceLabel;
    private final String departDateLabel;
    private final String returnDateLabel;
    private final String departLabel;
    private final String arriveLabel;
    private final String airlineLabel;
    private final String hotelLabel;
    private final String tripLabel;
}

