package renewal.awesome_travel.purchase.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.product.dto.responseDto.ProductResponseDto;
import renewal.common.entity.BasePurchase.PurchaseStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductPurchaseResponseDto {

    private Long id;

    private ProductResponseDto productResponseDto;

    private PurchaseStatus status;

    private Integer price;

    private Long userId;

    private String name;

    private String number;

    private String email;

    private LocalDateTime purchaseDate;

    private LocalDateTime paymentDueDate;

    private List<ProductPassengerResponseDto> productPassengers; // 탑승자 정보
}
