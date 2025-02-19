package renewal.awesome_travel.air.mapper;

import renewal.awesome_travel.air.dto.AirDto;
import renewal.awesome_travel.air.dto.SeatClassDto;
import renewal.awesome_travel.air.entity.Air;

import java.util.List;
import java.util.stream.Collectors;

public class AirMapper {


    public Air toAir(AirDto airDto) {
        if (airDto == null) {
            return null;
        }

        return new Air(airDto.getCode(), airDto.getAirline(), airDto.getDepart(),airDto.getDepart_time(),airDto.getArrive(),airDto.getArrive_time());
    }

    public static AirDto toAirDto(Air air) {
        if (air == null) {
            return null;
        }

        List<SeatClassDto> seatClassDtos = air.getSeatClasses().stream()
                .map(SeatClassMapper::toSeatClassDto)
                .collect(Collectors.toList());

        return new AirDto(air.getId(), air.getCode(), air.getAirline(), air.getDepart(), air.getDepart_time(), air.getArrive(), air.getArrive_time(), seatClassDtos);
    }
}
