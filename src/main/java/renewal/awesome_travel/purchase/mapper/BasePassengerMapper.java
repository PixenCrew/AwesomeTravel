package renewal.awesome_travel.purchase.mapper;

import renewal.awesome_travel.purchase.dto.CountryDto;
import renewal.awesome_travel.purchase.dto.SpecialRequestDto;
import renewal.awesome_travel.purchase.entity.*;
import renewal.awesome_travel.purchase.repository.CountryRepository;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class BasePassengerMapper {
    // 공통 DTO → 엔티티 변환 로직
    protected static <T extends BasePassenger> T mapToEntity(
            T passenger,
            String countryName,
            Set<SpecialRequestDto> specialRequestsDto,
            CountryRepository countryRepository
    ) {
        Country nationality = countryRepository.findByCountryName(countryName)
                .orElseThrow(() -> new RuntimeException("해당 국가를 찾을 수 없습니다: " + countryName));

        Set<SpecialRequest> specialRequests = specialRequestsDto != null
                ? specialRequestsDto.stream()
                .map(SpecialRequestMapper::toEntity)
                .collect(Collectors.toSet())
                : Collections.emptySet();

        passenger.setNationality(nationality);
        if (passenger instanceof AirPassenger airPassenger) {
            airPassenger.addSpecialRequests(specialRequests);
        } else if (passenger instanceof ProductPassenger productPassenger) {
            productPassenger.addSpecialRequests(specialRequests);
        }
        return passenger;
    }

    // 공통 엔티티 → DTO 변환 로직
    protected static <T extends BasePassenger> CountryDto mapCountryToDto(T passenger) {
        Country nationality = passenger.getNationality();
        return nationality != null
                ? new CountryDto(nationality.getCountryCode(),
                nationality.getCountryName(),
                nationality.getCountryNameLocal())
                : null;
    }

    protected static <T extends BasePassenger> Set<SpecialRequestDto> mapSpecialRequestsToDto(T passenger) {
        if (passenger instanceof AirPassenger airPassenger) {
            return airPassenger.getSpecialRequests() != null
                    ? airPassenger.getSpecialRequests().stream()
                    .map(SpecialRequestMapper::toDto)
                    .collect(Collectors.toSet())
                    : Collections.emptySet();
        } else if (passenger instanceof ProductPassenger productPassenger) {
            return productPassenger.getSpecialRequests() != null
                    ? productPassenger.getSpecialRequests().stream()
                    .map(SpecialRequestMapper::toDto)
                    .collect(Collectors.toSet())
                    : Collections.emptySet();
        }
        return Collections.emptySet();
    }
}
