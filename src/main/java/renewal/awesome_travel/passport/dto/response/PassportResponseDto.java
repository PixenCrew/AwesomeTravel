package renewal.awesome_travel.passport.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.purchase.dto.CountryDto;
import renewal.awesome_travel.purchase.utiles.Sex;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PassportResponseDto {

    private Long id;

    private LocalDate birth;

    private Sex sex;

    private CountryDto nationality;

    private String passport_num;

    private String lastName;

    private String firstName;

    private LocalDate expire;
}
