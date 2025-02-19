package renewal.awesome_travel.purchase.mapper;

import renewal.awesome_travel.purchase.dto.requestDto.AirPassengerRequestDto;
import renewal.awesome_travel.purchase.dto.responseDto.AirPassengerResponseDto;
import renewal.awesome_travel.purchase.dto.responseDto.AirPurchaseResponseDto;
import renewal.awesome_travel.purchase.entity.AirPassenger;
import renewal.awesome_travel.purchase.entity.AirPurchase;
import renewal.awesome_travel.purchase.repository.AirPurchaseRepository;
import renewal.awesome_travel.purchase.repository.CountryRepository;

public class AirPassengerMapper extends BasePassengerMapper {

    public static AirPassenger toEntity(AirPassengerRequestDto dto, CountryRepository countryRepository, AirPurchaseRepository airPurchaseRepository) {
        if (dto == null) {
            return null;
        }
        AirPurchase airPurchase = airPurchaseRepository.findByIdWithAll(dto.getAir_purchase_id())
                .orElseThrow(() -> new RuntimeException("해당 구매내역을 찾을 수 없습니다. " + dto.getAir_purchase_id()));

        AirPassenger airPassenger = new AirPassenger(airPurchase,
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

        return mapToEntity(airPassenger, dto.getCountryName(), dto.getSpecialRequests(), countryRepository);
    }
    public static AirPassengerResponseDto toDto(AirPassenger airPassenger) {
        if (airPassenger == null) {
            return null;
        }
        AirPurchase airPurchase = airPassenger.getAirPurchase();
        AirPurchaseResponseDto airPurchaseResponseDto =
                airPurchase != null ? AirPurchaseMapper.toDto(airPurchase) : null;

        return new AirPassengerResponseDto(
                airPassenger.getId(),
                airPurchaseResponseDto,
                airPassenger.getName(),
                airPassenger.getNumber(),
                airPassenger.getEmail(),
                airPassenger.getBirth(),
                airPassenger.getSex(),
                mapCountryToDto(airPassenger),
                airPassenger.getPassport_num(),
                airPassenger.getLastName(),
                airPassenger.getFirstName(),
                airPassenger.getExpire(),
                mapSpecialRequestsToDto(airPassenger)
        );
    }


}
