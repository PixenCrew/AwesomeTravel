package renewal.awesome_travel.air.repository;

import java.time.LocalDate;
import java.util.List;

import renewal.common.entity.AirportCode;
import renewal.common.entity.SeatClass;
import renewal.common.entity.SeatClass.SeatClassType;

public interface AirRepositoryCustom {
    // Page<?> search(AirSearchRequestDto req);
    List<SeatClass> searchSegment(
        Long seatCount,
        SeatClassType seatClassType,
        Boolean directOnly,
        AirportCode depart,
        AirportCode arrive,
        LocalDate departDate
        );
}
