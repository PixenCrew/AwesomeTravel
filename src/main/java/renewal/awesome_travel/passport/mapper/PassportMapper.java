package renewal.awesome_travel.passport.mapper;

import renewal.awesome_travel.passport.dto.request.PassportRequestDto;
import renewal.awesome_travel.passport.dto.response.PassportResponseDto;
import renewal.awesome_travel.passport.entity.Passport;
import renewal.awesome_travel.purchase.dto.CountryDto;
import renewal.awesome_travel.purchase.entity.Country;

public class PassportMapper {

    public static Passport toEntity(PassportRequestDto passportRequestDto, Country country) {
        if (passportRequestDto == null) {
            return null;
        }

        return new Passport(
                passportRequestDto.getBirth(),
                passportRequestDto.getSex(),
                country,
                passportRequestDto.getPassport_num(),
                passportRequestDto.getLastName(),
                passportRequestDto.getFirstName(),
                passportRequestDto.getExpire()
        );
    }

    public static PassportResponseDto toDto(Passport passport) {
        if (passport == null) {
            return null;
        }

        Country nationality = passport.getNationality();
        CountryDto countryDto = nationality != null
                ? new CountryDto(nationality.getCountryCode(),
                nationality.getCountryName(),
                nationality.getCountryNameLocal())
                : null;

        return new PassportResponseDto(
                passport.getId(),
                passport.getBirth(),
                passport.getSex(),
                countryDto,
                passport.getPassport_num(),
                passport.getLastName(),
                passport.getFirstName(),
                passport.getExpire()
        );
    }
}
