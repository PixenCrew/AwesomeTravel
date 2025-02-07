package renewal.awesome_travel.air.mapper;

import renewal.awesome_travel.air.dto.AirDto;
import renewal.awesome_travel.air.entity.Air;

public class AirMapper {


    public Air toAir(AirDto airDto) {
        if (airDto == null) {
            return null;
        }

        return new Air(airDto.getCode(), airDto.getAirline(), airDto.getPrice(), airDto.getDepart(),airDto.getDepart_time(),airDto.getArrive(),airDto.getArrive_time(),airDto.getMax(), airDto.getRest());
    }

    public AirDto toAirDto(Air air) {
        if (air == null) {
            return null;
        }

        return new AirDto(air.getId(), air.getCode(), air.getAirline(), air.getPrice(), air.getDepart(), air.getDepart_time(), air.getArrive(), air.getArrive_time(), air.getMax(), air.getRest());
    }
}
