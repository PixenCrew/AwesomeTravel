package renewal.awesome_travel.product.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.common.entity.Product;
import renewal.common.entity.Product.DepartTimeType;
import renewal.common.entity.Product.ProductStatus;
import renewal.common.entity.TimeDeal.DiscountType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductCalanderDto {

    private Long id;

    private String title;

    private DepartTimeType departDateTimeType;
    private LocalDateTime departDateTime;
    private LocalDateTime returnDateTime;

    private Long price;
    private Long remainSeats;
    private ProductStatus status;

    // 타임딜 정보
    private Long originalPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private DiscountType discountType;
    private Long discountValue;

    public ProductCalanderDto(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.departDateTimeType = product.getDepartTimeType();
        this.departDateTime = product.getDepartDateTime();
        this.returnDateTime = product.getReturnDateTime();
        this.price = product.getFinalPriceAdult();
        this.remainSeats = product.getAvailableSeats();
        this.status = product.getProductStatus();

        // 타임딜 해당 상품인경우 할인가격
        if (product.getTimeDeal() != null && product.getTimeDeal().isActive()) {
            this.originalPrice = product.getTimeDeal().getOriginalPriceAdult();
            this.discountType = product.getTimeDeal().getDiscountType();
            this.discountValue = product.getTimeDeal().getValue();
            this.startTime = product.getTimeDeal().getStartTime();
            this.endTime = product.getTimeDeal().getEndTime();
        }
    }

}
