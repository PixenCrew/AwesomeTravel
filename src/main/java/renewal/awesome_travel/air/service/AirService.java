package renewal.awesome_travel.air.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import renewal.awesome_travel.air.dto.request.AirSearchRequestDto;
import renewal.awesome_travel.air.repository.AirRepositoryCustom;
@Service
@RequiredArgsConstructor
public class AirService {

    private final AirRepositoryCustom airRepositoryCustom;

    public Page<?> searchFlights(AirSearchRequestDto req) {
        return airRepositoryCustom.search(req);
    }
}