package renewal.awesome_travel.air.repository;

import org.springframework.data.domain.Page;
import renewal.awesome_travel.air.dto.request.AirSearchRequestDto;

public interface AirRepositoryCustom {
    Page<?> search(AirSearchRequestDto req);
}
