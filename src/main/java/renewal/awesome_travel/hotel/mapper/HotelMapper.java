package renewal.awesome_travel.hotel.mapper;

import renewal.awesome_travel.hotel.dto.HotelDto;
import renewal.awesome_travel.hotel.entity.Hotel;

public class HotelMapper {

    public static Hotel toHotel(HotelDto hotelDto) {
        if (hotelDto == null) {
            return null;
        }

        return new Hotel(hotelDto.getName(), hotelDto.getDescription(), hotelDto.getAddress(), hotelDto.getNumber(), hotelDto.getEmail(), hotelDto.getWebsite(), hotelDto.getPrice(), hotelDto.getCheckIn(), hotelDto.getCheckOut(), hotelDto.getHotelType(), hotelDto.getRoomType());
    }

    public static HotelDto toHotelDto(Hotel hotel) {
        if (hotel == null) {
            return null;
        }

        return new HotelDto(hotel.getId(),
                hotel.getName(),
                hotel.getDescription(),
                hotel.getAddress(),
                hotel.getNumber(),
                hotel.getEmail(),
                hotel.getWebsite(),
                hotel.getPrice(),
                hotel.getCheckIn(),
                hotel.getCheckOut(),
                hotel.getHotelType(),
                hotel.getRoomType());
    }
}
