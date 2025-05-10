package renewal.awesome_travel.member.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UserUpdateRequestDto {

    private String name;
    private String phone;
    private LocalDate birthDate;

    // 여권 정보
    private String passportNumber;
    private LocalDate passportIssuedDate;
    private LocalDate passportExpiryDate;
    private String passportCountry;
    private String englishFirstName;
    private String englishLastName;

    private Boolean marketingConsent;
}

