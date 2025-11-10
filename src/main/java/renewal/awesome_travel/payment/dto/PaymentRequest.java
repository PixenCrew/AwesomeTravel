package renewal.awesome_travel.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long purchaseId;
    private String paymentMethod; // "CARD", "BANK", "ETC"
}
