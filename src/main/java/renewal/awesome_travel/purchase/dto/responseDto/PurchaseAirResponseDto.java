package renewal.awesome_travel.purchase.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.air.dto.response.AirResponseDto;
import renewal.common.entity.PurchaseBase.PurchaseStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseAirResponseDto {

    private Long id;

    private AirResponseDto airDto; // 기존 AirDto → AirResponseDto 로 변경

    private PurchaseStatus status;

    private Long price;

    private Long userId;

    private String name;

    private String number;

    private String email;

    private LocalDateTime purchaseDate;

    private LocalDateTime paymentDueDate;

    private List<PassengerAirResponseDto> airPassengers;
}

