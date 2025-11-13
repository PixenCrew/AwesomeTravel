package renewal.awesome_travel.air.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import renewal.awesome_travel.air.dto.AirSearchRequestDto.TripType;

@Getter
@Setter
public class AirDetailRequestDto {

    private TripType tripType;

    private Integer adultCount;
    private Integer youthCount;
    private Integer infantCount;

    private String seatClassType;
    private Boolean directOnly;

    private List<Long> seatClassIds; // seat class ID 목록
}
