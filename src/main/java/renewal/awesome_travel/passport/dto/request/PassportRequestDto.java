package renewal.awesome_travel.passport.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.purchase.dto.CountryDto;
import renewal.common.entity.PassengerBase.Sex;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PassportRequestDto {

    private LocalDate birth;

    private Sex sex;

    private String  countryName;

    private String passportNum;

    private String lastName;

    private String firstName;

    private LocalDate expire;
}
