package renewal.awesome_travel.air.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.common.entity.SeatClass.SeatClassType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SeatClassDto {

    private Long id;

    private Long airId;

    private SeatClassType seatClassType;

    private Integer price;

    private Integer maxSeats;

    private Integer availableSeats;


}
