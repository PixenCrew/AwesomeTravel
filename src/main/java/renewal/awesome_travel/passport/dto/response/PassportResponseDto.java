package renewal.awesome_travel.passport.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.purchase.dto.CountryDto;
import renewal.common.entity.Passenger.Sex;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PassportResponseDto {

    private Long id;

    private LocalDate birth;

    private Sex sex;

    private CountryDto nationality;

    private String passportNum;

    private String lastName;

    private String firstName;

    private LocalDate expire;
}
