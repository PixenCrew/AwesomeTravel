package renewal.awesome_travel.passport.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.passport.entity.Passport;
import renewal.common.entity.Passenger.AgeGroup;
import renewal.common.entity.Passenger.Sex;

@Getter
@Setter
@NoArgsConstructor
public class PassportDto {
    private Long id; // 수정 시 사용
    private String passportNum;
    private String lastName;
    private String firstName;
    private String lastNameKor;
    private String firstNameKor;
    private LocalDate birth;
    private Sex sex;

    private String countryCode;
    private String nationality;
    private String authority;
    private LocalDate issue;
    private LocalDate expire;

    private String email;
    private String number;
    private String specialRequests;
    private AgeGroup ageGroup;

    public static PassportDto from(Passport p) {
        PassportDto dto = new PassportDto();
        dto.setId(p.getId());
        dto.setPassportNum(p.getPassportNum());
        dto.setLastName(p.getLastName());
        dto.setFirstName(p.getFirstName());
        dto.setLastNameKor(p.getLastNameKor());
        dto.setFirstNameKor(p.getFirstNameKor());
        dto.setBirth(p.getBirth());
        dto.setSex(p.getSex());
        dto.setCountryCode(p.getCountryCode().getCode());
        dto.setNationality(p.getNationality());
        dto.setAuthority(p.getAuthority());
        dto.setIssue(p.getIssue());
        dto.setExpire(p.getExpire());
        return dto;
    }
}
