package renewal.awesome_travel.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatePassportRequestDto {
    private Long id;
    private String passportNum;
    private String lastName;
    private String firstName;
    private String lastNameKor;
    private String firstNameKor;
    private String birth;
    private String sex;

    private String countryCode;
    private String nationality;
    private String authority;
    private String issue;
    private String expire;

    private String email;
    private String number;
}
