package renewal.awesome_travel.product.mapper;

import org.springframework.stereotype.Component;
import renewal.awesome_travel.air.entity.Air;
import renewal.awesome_travel.air.mapper.AirMapper;
import renewal.awesome_travel.hotel.entity.Hotel;
import renewal.awesome_travel.hotel.mapper.HotelMapper;
import renewal.awesome_travel.product.dto.requestDto.ProductListRequestDto;
import renewal.awesome_travel.product.dto.responseDto.ProductListResponseDto;
import renewal.awesome_travel.product.entity.Product;
import renewal.awesome_travel.product.entity.ProductList;

@Component
public class ProductListMapper {

    public static ProductList toProductList(ProductListRequestDto productListRequestDto, Product product, Air air, Hotel hotel) {
        if (productListRequestDto == null) {
            return null;
        }

        return new ProductList(
                product,
                air,
                hotel,
                productListRequestDto.getDate(),
                productListRequestDto.getPrice(),
                productListRequestDto.getCurrentReserve()
        );


    }

    public static ProductListResponseDto toProductListDto(ProductList productList) {
        if (productList == null) {
            return null;
        }

        return new ProductListResponseDto(
                productList.getId(),
                productList.getProduct() != null ? ProductMapper.toProductDto(productList.getProduct()) : null, // Product 변환
                productList.getAir() != null ? AirMapper.toAirDto(productList.getAir()) : null, // Air 변환
                productList.getHotel() != null ? HotelMapper.toHotelDto(productList.getHotel()) : null, // Hotel 변환
                productList.getDate(),
                productList.getPrice(),
                productList.getCurrentReserve()
        );
    }
}
