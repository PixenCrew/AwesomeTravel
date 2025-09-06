package renewal.awesome_travel.purchase.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AirPurchaseRequestDto {

    private Long seatClassId;

    private int adultCount;   // 성인 인원수
    private int childCount;   // 소아 인원수
    private int infantCount;  // 유아 인원수

    private Long userId;

    private String name;

    private String number;

    private String email;

    private LocalDateTime purchaseDate;

    private LocalDateTime paymentDueDate;

    private List<AirPassengerRequestDto> passengers; // 탑승자 정보
}
