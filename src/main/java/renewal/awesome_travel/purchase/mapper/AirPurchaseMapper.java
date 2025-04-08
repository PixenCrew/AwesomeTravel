package renewal.awesome_travel.purchase.mapper;

import renewal.awesome_travel.air.dto.response.AirDto;
import renewal.awesome_travel.air.entity.Air;
import renewal.awesome_travel.air.mapper.SeatClassMapper;
import renewal.awesome_travel.purchase.dto.requestDto.AirPurchaseRequestDto;
import renewal.awesome_travel.purchase.dto.responseDto.AirPassengerResponseDto;
import renewal.awesome_travel.purchase.dto.responseDto.AirPurchaseResponseDto;
import renewal.awesome_travel.purchase.entity.AirPassenger;
import renewal.awesome_travel.purchase.entity.AirPurchase;
import renewal.awesome_travel.purchase.repository.AirPurchaseRepository;
import renewal.awesome_travel.purchase.repository.CountryRepository;

import java.util.List;
import java.util.stream.Collectors;

public class AirPurchaseMapper {

    public static AirPurchase toEntity(AirPurchaseRequestDto dto, Air air, CountryRepository countryRepository, AirPurchaseRepository airPurchaseRepository) {
        if (dto == null) {
            return null;
        }
        // 탑승자 정보 변환 (AirPassengerDto → AirPassenger)
        List<AirPassenger> passengers = dto.getAirPassengers() != null
                ? dto.getAirPassengers().stream()
                .map(passengerDto -> AirPassengerMapper.toEntity(passengerDto, countryRepository, airPurchaseRepository))
                .collect(Collectors.toList())
                : null;

        AirPurchase airPurchaseResult = new AirPurchase(
                air,
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
            airPurchaseResult.getAirPassengers().addAll(passengers);
        }

        return airPurchaseResult;
    }

    public static AirPurchaseResponseDto toDto(AirPurchase airPurchase) {
        if (airPurchase == null) {
            return null;
        }

        // Air 엔티티를 AirDto로 변환
        Air air = airPurchase.getAir();
        AirDto airDto = air != null
                ? new AirDto(
                air.getId(),
                air.getCode(),
                air.getAirline(),
                air.getDepart(),
                air.getDepart_time(),
                air.getArrive(),
                air.getArrive_time(),
                air.getSeatClasses().stream().map(SeatClassMapper::toSeatClassDto).collect(Collectors.toList())
        )
                : null; // Air가 없는 경우 null 처리

        List<AirPassengerResponseDto> passengerResponseDtos = airPurchase.getAirPassengers() != null
                ? airPurchase.getAirPassengers().stream()
                    .map(AirPassengerMapper::toDto)
                    .collect(Collectors.toList())
                : null;

        return new AirPurchaseResponseDto(
                airPurchase.getId(),
                airDto,
                airPurchase.getStatus(),
                airPurchase.getPrice(),
                airPurchase.getMember_id(),
                airPurchase.getName(),
                airPurchase.getNumber(),
                airPurchase.getEmail(),
                airPurchase.getPurchaseDate(),
                airPurchase.getPaymentDueDate(),
                passengerResponseDtos
        );
    }
}
