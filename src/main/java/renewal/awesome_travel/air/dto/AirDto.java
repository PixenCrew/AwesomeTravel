package renewal.awesome_travel.air.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AirDto {

    private Long id;

    private String code;

    private String airline;

    private Integer price;

    private String depart;

    private LocalDateTime depart_time;

    private String arrive;

    private LocalDateTime arrive_time;

    private Integer max;

    private Integer rest;
}
