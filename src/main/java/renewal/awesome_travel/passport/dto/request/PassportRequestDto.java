package renewal.awesome_travel.passport.dto.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.common.entity.Passenger.Sex;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PassportRequestDto {

    private LocalDate birth;

    private Sex sex;

    private String countryName;

    private String passportNum;

    private String lastName;

    private String firstName;

    private LocalDate expire;
}
