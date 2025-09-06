package renewal.awesome_travel.air.dto.response;

import lombok.Builder;
import lombok.Getter;
import renewal.common.entity.SeatClass.SeatClassType;

@Getter
@Builder
public class SeatClassDetailResponseDto {
    private Long seatClassId;

    private String airCode;
    private String airlineName;

    private String depart;
    private String arrive;
    private String departTime;
    private String arriveTime;

    private SeatClassType classType;
    private int price;
    private int availableSeats;
}
