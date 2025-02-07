package renewal.awesome_travel.event.mapper;

import org.springframework.stereotype.Component;
import renewal.awesome_travel.event.dto.EventDto;
import renewal.awesome_travel.event.entity.Event;

@Component
public class EventMapper {

    public Event toEvent(EventDto eventDto) {
        if (eventDto == null) {
            return null;
        }

        return new Event(eventDto.getName(), eventDto.getType(), eventDto.getStartDate(), eventDto.getEndDate(), eventDto.getFile(), eventDto.getContent());
    }

    public EventDto toEventDto(Event event) {
        if (event == null) {
            return null;
        }

        return new EventDto(event.getId(), event.getName(), event.getType(), event.getStartDate(), event.getEndDate(), event.getFile(), event.getContent(), event.getEventProducts());
    }
}
