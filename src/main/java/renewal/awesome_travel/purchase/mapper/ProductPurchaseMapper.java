package renewal.awesome_travel.purchase.mapper;


import renewal.awesome_travel.product.dto.responseDto.ProductResponseDto;
import renewal.awesome_travel.product.entity.Product;
import renewal.awesome_travel.product.mapper.ProductMapper;
import renewal.awesome_travel.purchase.dto.requestDto.ProductPurchaseRequestDto;
import renewal.awesome_travel.purchase.dto.responseDto.ProductPassengerResponseDto;
import renewal.awesome_travel.purchase.dto.responseDto.ProductPurchaseResponseDto;
import renewal.awesome_travel.purchase.entity.ProductPassenger;
import renewal.awesome_travel.purchase.entity.ProductPurchase;
import renewal.awesome_travel.purchase.repository.CountryRepository;
import renewal.awesome_travel.purchase.repository.ProductPurchaseRepository;

import java.util.List;
import java.util.stream.Collectors;

public class ProductPurchaseMapper {

    public static ProductPurchase toEntity(ProductPurchaseRequestDto dto, Product product, CountryRepository countryRepository, ProductPurchaseRepository productPurchaseRepository) {
        if (dto == null) {
            return null;
        }
        // 탑승자 정보 변환 (ProductPassengerDto → ProductPassenger)
        List<ProductPassenger> passengers = dto.getProductPassengers() != null
                ? dto.getProductPassengers().stream()
                .map(passengerDto -> ProductPassengerMapper.toEntity(passengerDto, countryRepository, productPurchaseRepository))
                .collect(Collectors.toList())
                : null;

        ProductPurchase productPurchaseResult = new ProductPurchase(
                product,
                dto.getStatus(),
                dto.getPrice(),
                dto.getMember_id(),
                dto.getName(),
                dto.getNumber(),
                dto.getEmail(),
                dto.getPurchaseDate(),
                dto.getPaymentDueDate()
        );

        if (passengers != null) {
            productPurchaseResult.getProductPassengers().addAll(passengers);
        }

        return productPurchaseResult;
    }

    public static ProductPurchaseResponseDto toDto(ProductPurchase productPurchase) {
        if (productPurchase == null) {
            return null;
        }

        // Product 엔티티를 ProductDto로 변환
        Product product = productPurchase.getProduct();
        ProductResponseDto productResponseDto = product != null
                ? ProductMapper.toProductDto(product)
                : null; // Product가 없는 경우 null 처리

        List<ProductPassengerResponseDto> passengerResponseDtos = productPurchase.getProductPassengers() != null
                ? productPurchase.getProductPassengers().stream()
                .map(ProductPassengerMapper::toDto)
                .collect(Collectors.toList())
                : null;

        return new ProductPurchaseResponseDto(
                productPurchase.getId(),
                productResponseDto,
                productPurchase.getStatus(),
                productPurchase.getPrice(),
                productPurchase.getMember_id(),
                productPurchase.getName(),
                productPurchase.getNumber(),
                productPurchase.getEmail(),
                productPurchase.getPurchaseDate(),
                productPurchase.getPaymentDueDate(),
                passengerResponseDtos
        );
    }
}
