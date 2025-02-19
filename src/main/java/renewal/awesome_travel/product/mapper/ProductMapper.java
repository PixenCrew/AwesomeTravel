package renewal.awesome_travel.product.mapper;

import org.springframework.stereotype.Component;
import renewal.awesome_travel.product.dto.requestDto.ProductRequestDto;
import renewal.awesome_travel.product.dto.responseDto.ProductResponseDto;
import renewal.awesome_travel.product.entity.Product;
import renewal.awesome_travel.product.entity.ProductList;
import renewal.awesome_travel.product.entity.Tag;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public static Product toProduct(ProductRequestDto productRequestDto, List<Tag> tags, List<ProductList> productLists) {
        if (productRequestDto == null) {
            return null;
        }

        Product product = new Product(
                productRequestDto.getName(),
                productRequestDto.getDepart(),
                productRequestDto.getDestination(),
                productRequestDto.getDuration(),
                productRequestDto.getMax_reserve(),
                productRequestDto.getDescription()
        );

        // 태그 ID를 이용하여 태그 엔티티 조회 후 추가
        if (tags != null) {
            product.getTags().addAll(tags);
        }

        // 상품 리스트 변환하여 추가
        if (productLists != null) {
            product.getProductLists().addAll(productLists);
        }

        return product;
    }

    public static ProductResponseDto toProductDto(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDepart(),
                product.getDestination(),
                product.getDuration(),
                product.getMax_reserve(),
                product.getDescription(),
                product.getView(), // 조회수는 응답에서만 반환
                product.getProductLists() != null
                        ? product.getProductLists().stream().map(ProductListMapper::toProductListDto).collect(Collectors.toList())
                        : null,
                product.getTags() != null
                        ? product.getTags().stream().map(TagMapper::toTagDto).collect(Collectors.toList())
                        : null
        );
    }


}
