package renewal.awesome_travel.air.dto.response;

import lombok.Builder;
import lombok.Getter;
import renewal.common.entity.Air;
// import renewal.awesome_travel.air.mapper.AirMapper;
import renewal.common.entity.SeatClass.SeatClassType;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Builder
public class MultiTripSearchResultDto {

    private List<AirResponseDto> segments;
    private int totalPrice;

    public static MultiTripSearchResultDto of(List<Air> flights, SeatClassType seatClassType) {
        if (flights == null || flights.isEmpty()) return null;

        List<AirResponseDto> segmentDtos = flights.stream()
                .map(air -> AirMapper.toAirResponseDto(air, seatClassType))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (segmentDtos.size() != flights.size()) return null; // 변환 실패한 항공이 있는 경우

        int total = segmentDtos.stream().mapToInt(AirResponseDto::getPrice).sum();

        return MultiTripSearchResultDto.builder()
                .segments(segmentDtos)
                .totalPrice(total)
                .build();
    }
}
