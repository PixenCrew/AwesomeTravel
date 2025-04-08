package renewal.awesome_travel.air.domain;

import lombok.Getter;
import renewal.awesome_travel.air.entity.Air;
import renewal.awesome_travel.air.utiles.SeatClassType;
import java.util.List;

@Getter
public class MultiTripSearchResult {

    private final List<Air> flights;
    private final int totalPrice;

    public MultiTripSearchResult(List<Air> flights, SeatClassType seatClassType) {
        this.flights = flights;
        this.totalPrice = calculateTotalPrice(flights, seatClassType);
    }

    private int calculateTotalPrice(List<Air> flights, SeatClassType seatClassType) {
        return flights.stream()
                .mapToInt(air -> air.getPrice(seatClassType))
                .sum();
    }
}
