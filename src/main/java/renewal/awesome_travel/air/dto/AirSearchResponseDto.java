package renewal.awesome_travel.air.dto;

import java.time.LocalDateTime;
import java.util.List;

import renewal.common.entity.CityCode;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AirSearchResponseDto {

    private Long price; // 항공권 가격

    private List<Trip> tripList; // 여정 리스트

    @Getter
    @Setter
    public class Trip {

        // private Long totalDuration; // 여정 소요시간 => (첫 출발시간 - 마지막 도착시간 차이로 계산?)
        private List<FlightSegment> flightSegments; // 비행 리스트

        @Getter
        @Setter
        public class FlightSegment {

            private CityCode departAirport; // 출발공항
            private String departTerminal; // 출발터미널
            private LocalDateTime departTime; // 출발날짜시간

            // private Long flightDuration; // 비행시간 => (도착시간 - 출발시간 으로 계산?)

            private CityCode arriveAirport; // 도착공항
            private String arriveTerminal; // 도착터미널
            private LocalDateTime arriveTime; // 도착날짜시간

            // private Long waitDuration; // 경유인 경우(=flightSegments 길이가 2 이상) 다음
            // FlightSegment 전까지 대기시간
            // => (전 도착시간 - 다음 출발시간 차이로 계산?)

        }
    }

}
