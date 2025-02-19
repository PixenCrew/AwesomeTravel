package renewal.awesome_travel.air.mapper;

import renewal.awesome_travel.air.dto.SeatClassDto;
import renewal.awesome_travel.air.entity.Air;
import renewal.awesome_travel.air.entity.SeatClass;

public class SeatClassMapper {

    public SeatClass toSeatClass(SeatClassDto seatClassDto) {
        if (seatClassDto == null) {
            return null;
        }
        //air는 서비스단에서 설정

        return new SeatClass(seatClassDto.getSeatClassType(), seatClassDto.getPrice(), seatClassDto.getMaxSeats(), seatClassDto.getAvailableSeats());
    }

    public static SeatClassDto toSeatClassDto(SeatClass seatClass) {
        if (seatClass == null) {
            return null;
        }

        return new SeatClassDto(
                seatClass.getId(),
                seatClass.getAir().getId(),
                seatClass.getClassType(),
                seatClass.getPrice(),
                seatClass.getMaxSeats(),
                seatClass.getAvailableSeats());
    }
}
