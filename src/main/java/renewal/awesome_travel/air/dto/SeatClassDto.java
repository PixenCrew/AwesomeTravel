package renewal.awesome_travel.air.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.air.entity.Air;
import renewal.awesome_travel.air.utiles.SeatClassType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SeatClassDto {

    private Long id;

    private Long air_id;

    private SeatClassType seatClassType;

    private Integer price;

    private Integer maxSeats;

    private Integer availableSeats;


}
