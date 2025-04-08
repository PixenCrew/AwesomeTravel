package renewal.awesome_travel.air.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.air.dto.SeatClassDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AirDto {

    private Long id;

    private String code;

    private String airline;

    private String depart;

    private String depart_time;

    private String arrive;

    private String arrive_time;

    private List<SeatClassDto> seatClasses;
}
