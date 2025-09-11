package renewal.awesome_travel.event.dto;

import java.time.LocalDate;
import java.util.List;

import renewal.awesome_travel.event.entity.EventProduct;
import renewal.awesome_travel.event.entity.Event.EventType;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDto {
    private Long id;
    private String name;
    private EventType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String file;
    private String content;
    private List<EventProduct> eventProducts;
}