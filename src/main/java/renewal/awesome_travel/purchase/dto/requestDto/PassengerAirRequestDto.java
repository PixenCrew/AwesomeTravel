package renewal.awesome_travel.purchase.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.common.entity.PassengerBase.Sex;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PassengerAirRequestDto {
    private String name;
    private String number;
    private String email;
    private LocalDate birth;
    private Sex sex;
    private String countryCode;
    private String passportNum;
    private String lastName;
    private String firstName;
    private LocalDate expire;
    private Set<Long> specialRequestIds;
}
