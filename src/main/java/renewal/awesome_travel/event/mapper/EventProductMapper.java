package renewal.awesome_travel.event.mapper;

import org.springframework.stereotype.Component;
import renewal.awesome_travel.event.dto.EventProductDto;
import renewal.awesome_travel.event.entity.EventProduct;

@Component
public class EventProductMapper {

    public EventProduct toEventProduct(EventProductDto eventProductDto) {
        if (eventProductDto == null) {
            return null;
        }

        return new EventProduct(eventProductDto.getEvent(), eventProductDto.getProduct(),eventProductDto.getPrice());

    }

    public EventProductDto toEventProductDto(EventProduct eventProduct) {
        if (eventProduct == null) {
            return null;
        }

        return new EventProductDto(eventProduct.getId(), eventProduct.getEvent(), eventProduct.getProduct(), eventProduct.getPrice());
    }
}
