package renewal.awesome_travel.product.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.air.dto.AirDto;
import renewal.awesome_travel.air.entity.Air;
import renewal.awesome_travel.hotel.dto.HotelDto;
import renewal.awesome_travel.hotel.entity.Hotel;
import renewal.awesome_travel.product.entity.Product;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductListResponseDto {

    private Long id;

    private ProductResponseDto product;

    private AirDto air;

    private HotelDto hotel;

    private String date;

    private Integer price;

    private Integer currentReserve;
}
