package renewal.awesome_travel.air.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class SegmentRequest {
    private String depart;
    private String arrive;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate departDate;
}

