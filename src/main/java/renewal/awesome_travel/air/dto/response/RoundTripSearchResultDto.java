package renewal.awesome_travel.air.dto.response;

import lombok.Builder;
import lombok.Getter;
import renewal.common.entity.Air;
// import renewal.awesome_travel.air.mapper.AirMapper;
import renewal.common.entity.SeatClass.SeatClassType;

@Getter
@Builder
public class RoundTripSearchResultDto {
    private AirResponseDto outbound;
    private AirResponseDto inbound;
    private int totalPrice;

    public static RoundTripSearchResultDto of(Air go, Air back, SeatClassType type) {
        AirResponseDto goDto = AirMapper.toAirResponseDto(go, type);
        AirResponseDto backDto = AirMapper.toAirResponseDto(back, type);

        if (goDto == null || backDto == null) return null;

        return RoundTripSearchResultDto.builder()
                .outbound(goDto)
                .inbound(backDto)
                .totalPrice(goDto.getPrice() + backDto.getPrice())
                .build();
    }

    public String getOutboundDepartTime() {
        return outbound.getDepartTime();
    }

    public String getInboundDepartTime() {
        return inbound.getDepartTime();
    }
}

