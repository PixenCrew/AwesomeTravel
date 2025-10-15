package renewal.awesome_travel.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.hotel.utiles.HotelType;
import renewal.awesome_travel.hotel.utiles.RoomType;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HotelDto {

    private Long id;

    private String name;

    private String description;

    private String address;

    private String number;

    private String email;

    private String website;

    private Integer price;

    private LocalDate checkIn;

    private LocalDate checkOut;

    private HotelType hotelType;

    private RoomType roomType;
}
