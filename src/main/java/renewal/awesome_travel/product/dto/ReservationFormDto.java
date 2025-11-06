package renewal.awesome_travel.product.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationFormDto {
    Long productId;
    LocalDate departDate;
    Long adult;
    Long youth;
    Long infant;
}
