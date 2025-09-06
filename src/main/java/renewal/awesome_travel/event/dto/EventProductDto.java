package renewal.awesome_travel.event.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.event.entity.Event;
import renewal.common.entity.Product;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventProductDto {

    private Long id;

    private Event event;

    private Product product;

    private Integer price;


}
