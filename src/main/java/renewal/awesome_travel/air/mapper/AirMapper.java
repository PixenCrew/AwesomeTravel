package renewal.awesome_travel.air.mapper;

import renewal.awesome_travel.air.dto.response.AirResponseDto;
import renewal.awesome_travel.air.entity.Air;
import renewal.awesome_travel.air.utiles.SeatClassType;

public class AirMapper {

    public static AirResponseDto toAirResponseDto(Air air, SeatClassType classType) {
        if (air == null || air.getSeatClasses() == null) return null;

        return air.getSeatClasses().stream()
                .filter(seat -> seat.getClassType() == classType)
                .findFirst()
                .map(seat -> AirResponseDto.builder()
                        .airId(air.getId())
                        .code(air.getCode())
                        .airlineCode(air.getAirline().getCode())
                        .airlineNameKor(air.getAirline().getNameKor())
                        .airlineNameEng(air.getAirline().getNameEng())
                        .depart(air.getDepart())
                        .arrive(air.getArrive())
                        .departTime(air.getDepart_time())
                        .arriveTime(air.getArrive_time())
                        .stopovers(air.getStopovers())
                        .flightType(air.getFlightType())

                        .seatClassId(seat.getId())
                        .seatClassType(seat.getClassType())
                        .price(seat.getPrice())
                        .availableSeats(seat.getAvailableSeats())
                        .build())
                .orElse(null);
    }
}

