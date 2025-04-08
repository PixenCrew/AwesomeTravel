package renewal.awesome_travel.product.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.air.dto.response.AirResponseDto;
import renewal.awesome_travel.hotel.dto.HotelDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductListResponseDto {

    private Long id;

    private ProductResponseDto product;

    private AirResponseDto air;

    private HotelDto hotel;

    private String date;

    private Integer price;

    private Integer currentReserve;
}
