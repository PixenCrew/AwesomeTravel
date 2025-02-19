package renewal.awesome_travel.purchase.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.purchase.utiles.Status;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductPurchaseRequestDto {

    private Long productId;

    private Status status;

    private Integer price;

    private Long member_id;

    private String name;

    private String number;

    private String email;

    private LocalDateTime purchaseDate;

    private LocalDateTime paymentDueDate;

    private List<ProductPassengerRequestDto> productPassengers; // 탑승자 정보
}
