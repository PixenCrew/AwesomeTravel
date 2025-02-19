package renewal.awesome_travel.hotel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.hotel.utiles.HotelType;
import renewal.awesome_travel.hotel.utiles.RoomType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Hotel")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hotel_id")
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

    public Hotel(String name, String description, String address, String number, String email, String website, Integer price, LocalDate checkIn, LocalDate checkOut, HotelType hotelType, RoomType roomType) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.number = number;
        this.email = email;
        this.website = website;
        this.price = price;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.hotelType = hotelType;
        this.roomType = roomType;
    }
}
