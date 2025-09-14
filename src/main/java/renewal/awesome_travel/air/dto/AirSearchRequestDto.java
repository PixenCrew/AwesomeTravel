package renewal.awesome_travel.air.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import renewal.awesome_travel.air.dto.AirSearchRequestDto.RequestSegment;
import renewal.common.entity.CityCode;
import renewal.common.entity.SeatClass.SeatClassType;

import lombok.Getter;
import lombok.Setter;

import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class AirSearchRequestDto {

    // 여행 타입: 
    private TripType tripType;

    // 인원수
    private Long adultCount;
    private Long childCount;
    private Long infantCount;

    // 좌석 등급 (이코노미, 비즈니스 등)
    private SeatClassType seatClassType;

    // 직항 여부
    private Boolean directOnly;

    // 구간 리스트
    private List<RequestSegment> segments = new ArrayList<>();

    @Getter
    @Setter
    public static class RequestSegment {
        private CityCode depart;
        private CityCode arrive;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate departDate;
    }

    public enum TripType{
        ROUND_TRIP,
        ONE_WAY,
        MULTI
    }
}
