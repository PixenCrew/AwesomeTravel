package renewal.awesome_travel.air.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import renewal.common.entity.Airline;
import renewal.common.entity.AirportCode;
import renewal.common.entity.CityCode;
import renewal.common.entity.Air.FlightSegment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class AirSearchResponseDto {

    private Long price; // 항공권 가격

    private List<Trip> tripList = new ArrayList<Trip>(); // 여정 리스트 [1구간, 2구간, 3구간 ...]

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Trip { // == SeatClass
        private Airline airline; // 항공사
        private String flightNumber; // 항공편 코드

        private AirportCode depart;
        private LocalDateTime departDateTime;

        private Long flightDurationTotal;
        private Integer stopOvers;

        private AirportCode arrive;
        private LocalDateTime arriveDateTime;

        // private Long totalDuration; // 여정 소요시간 => (첫 출발시간 - 마지막 도착시간 차이로 계산?)
        private List<FlightSegment> flightSegments; // 비행 리스트 [1구간 출발, 1구간 경유도착, 1구간 경유출발,... ]
    }
}
