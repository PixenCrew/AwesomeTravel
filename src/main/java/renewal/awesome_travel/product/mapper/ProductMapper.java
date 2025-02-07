package renewal.awesome_travel.product.mapper;

import org.springframework.stereotype.Component;
import renewal.awesome_travel.product.dto.ProductDto;
import renewal.awesome_travel.product.entity.Product;

@Component
public class ProductMapper {

    public Product productDtoToProduct(ProductDto productDto) {
        if (productDto == null) {
            return null;
        }

        return new Product(productDto.getName(), productDto.getDepart(), productDto.getDestination(), productDto.getDuration(), productDto.getMax_reserve(), productDto.getDescription());
    }

    public ProductDto productToProductDto(Product product) {
        if (product == null) {
            return null;
        }

        return new ProductDto(product.getId(), product.getName(), product.getDepart(), product.getDestination(), product.getDuration(), product.getMax_reserve(), product.getDescription(), product.getView(), product.getProductLists(), product.getTags());
    }


}
