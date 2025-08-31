package renewal.awesome_travel.purchase.mapper;

import renewal.awesome_travel.purchase.dto.requestDto.ProductPassengerRequestDto;
import renewal.awesome_travel.purchase.dto.responseDto.ProductPassengerResponseDto;
import renewal.awesome_travel.purchase.dto.responseDto.ProductPurchaseResponseDto;
import renewal.awesome_travel.purchase.entity.*;
import renewal.awesome_travel.purchase.repository.CountryRepository;
import renewal.awesome_travel.purchase.repository.ProductPurchaseRepository;
import renewal.common.entity.ProductPassenger;
import renewal.common.entity.ProductPurchase;

public class ProductPassengerMapper extends BasePassengerMapper {

    public static ProductPassenger toEntity(ProductPassengerRequestDto dto, CountryRepository countryRepository, ProductPurchaseRepository productPurchaseRepository) {
        if (dto == null) {
            return null;
        }
        ProductPurchase productPurchase = productPurchaseRepository.findByIdWithAll(dto.getProduct_purchase_id())
                .orElseThrow(() -> new RuntimeException("해당 구매내역을 찾을 수 없습니다. " + dto.getProduct_purchase_id()));

        ProductPassenger productPassenger = new ProductPassenger(productPurchase,
                dto.getName(),
                dto.getNumber(),
                dto.getEmail(),
                dto.getBirth(),
                dto.getSex(),
                null,
                dto.getPassport_num(),
                dto.getLastName(),
                dto.getFirstName(),
                dto.getExpire());

        return mapToEntity(productPassenger, dto.getCountryName(), dto.getSpecialRequests(), countryRepository);
    }

    public static ProductPassengerResponseDto toDto(ProductPassenger productPassenger) {
        if (productPassenger == null) {
            return null;
        }
        ProductPurchase productPurchase = productPassenger.getProductPurchase();
        ProductPurchaseResponseDto productPurchaseResponseDto =
                productPurchase != null ? ProductPurchaseMapper.toDto(productPurchase) : null;

        return new ProductPassengerResponseDto(
                productPassenger.getId(),
                productPurchaseResponseDto,
                productPassenger.getName(),
                productPassenger.getNumber(),
                productPassenger.getEmail(),
                productPassenger.getBirth(),
                productPassenger.getSex(),
                mapCountryToDto(productPassenger),
                productPassenger.getPassport_num(),
                productPassenger.getLastName(),
                productPassenger.getFirstName(),
                productPassenger.getExpire(),
                mapSpecialRequestsToDto(productPassenger)
        );
    }

}
