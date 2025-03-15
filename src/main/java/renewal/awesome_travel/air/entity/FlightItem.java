package renewal.awesome_travel.air.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlightItem {

    @JsonProperty("항공사")
    private String airline;

    @JsonProperty("운항편명")
    private String flightNumber;

    @JsonProperty("출발공항")
    private String departureAirport;

    @JsonProperty("도착공항")
    private String arrivalAirport;

    @JsonProperty("출발시간")
    private String departureTime;

    @JsonProperty("도착시간")
    private String arrivalTime;

    @JsonProperty("운항요일")
    private String operatingDays;

    @JsonProperty("시작일자")
    private String startDate;

    @JsonProperty("종료일자")
    private String endDate;

    @JsonProperty("국내_국제")
    private String domesticInternational;
}
