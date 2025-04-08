package renewal.awesome_travel.air.dto.response;

import lombok.Builder;
import lombok.Getter;
import renewal.awesome_travel.air.utiles.FlightType;
import renewal.awesome_travel.air.utiles.SeatClassType;

@Getter
@Builder
public class AirResponseDto {
    private Long airId;
    private String code;

    //항공사정보
    private String airlineCode;
    private String airlineNameKor;
    private String airlineNameEng;

    private String depart;
    private String arrive;
    private String departTime;
    private String arriveTime;
    private int stopovers;
    private FlightType flightType;

    // 단일 좌석 정보
    private Long seatClassId;
    private SeatClassType seatClassType;
    private Integer price;
    private Integer availableSeats;
}

