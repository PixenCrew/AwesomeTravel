package renewal.awesome_travel.passport.dto.request;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.common.entity.Passenger.AgeGroup;
import renewal.common.entity.Passenger.Sex;

@Getter
@Setter
@NoArgsConstructor
public class PassportDto {
    private Long id;
    private String name;
    private LocalDate birth;
    private Sex sex;
    private String number;
    private String email;
    private AgeGroup ageGroup;

    private String nationality;
    private String passportNum;
    private String lastName;
    private String firstName;
    private LocalDate expire;
    private String specialRequests;
}
