package renewal.awesome_travel.product.mapper;

import org.springframework.stereotype.Component;
import renewal.awesome_travel.product.dto.ProductListDto;
import renewal.awesome_travel.product.entity.ProductList;

@Component
public class ProductListMapper {

    public ProductList toProductList(ProductListDto productListDto) {
        if (productListDto == null) {
            return null;
        }

        return new ProductList(productListDto.getProduct(), productListDto.getAir_id(), productListDto.getHotel_id(), productListDto.getDate(), productListDto.getPrice(), productListDto.getCurrentReserve());


    }

    public ProductListDto toProductListDto(ProductList productList) {
        if (productList == null) {
            return null;
        }

        return new ProductListDto(productList.getId(), productList.getProduct(), productList.getAir_id(), productList.getHotel_id(), productList.getDate(), productList.getPrice(), productList.getCurrentReserve());

    }
}
