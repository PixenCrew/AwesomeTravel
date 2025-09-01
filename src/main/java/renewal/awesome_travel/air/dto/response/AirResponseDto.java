package renewal.awesome_travel.air.dto.response;

import java.time.LocalTime;

import lombok.Builder;
import lombok.Getter;
import renewal.common.entity.Air.FlightType;
import renewal.common.entity.SeatClass.SeatClassType;

@Getter
@Builder
public class AirResponseDto {
    private Long airId;
    // private String code;

    //항공사정보
    private String airlineCode;
    private String airlineNameKor;
    private String airlineNameEng;

    private String depart;
    private String arrive;
    private LocalTime departTime;
    private LocalTime arriveTime;
    private int stopovers;
    private FlightType flightType;

    // 단일 좌석 정보
    private Long seatClassId;
    private SeatClassType seatClassType;
    private Long price;
    private Long availableSeats;
}

