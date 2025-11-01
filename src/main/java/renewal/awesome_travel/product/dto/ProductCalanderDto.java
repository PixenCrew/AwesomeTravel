package renewal.awesome_travel.product.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.common.entity.Product;
import renewal.common.entity.Product.DepartTimeType;
import renewal.common.entity.Product.ProductStatus;

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

    public ProductCalanderDto(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.departDateTimeType = product.getDepartTimeType();
        this.departDateTime = product.getDepartDateTime();
        this.returnDateTime = product.getReturnDateTime();
        this.price = product.getFinalPriceAdult();
        this.remainSeats = product.getAvailableSeats();
        this.status = ProductStatus.AVAILABLE; // 예약가능, 예약대기 기능 구현시 변경
    }

}
