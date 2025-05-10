package renewal.awesome_travel.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import renewal.awesome_travel.member.entity.User;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class UserResponseDto {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private LocalDate birthDate;

    private String role;
    private String status;

    // 여권 정보
    private String passportNumber;
    private LocalDate passportIssuedDate;
    private LocalDate passportExpiryDate;
    private String passportCountry;
    private String englishFirstName;
    private String englishLastName;

    // 기타 정보
    private Boolean emailVerified;
    private Boolean marketingConsent;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.birthDate = user.getBirthDate();

        this.role = user.getRole().name();
        this.status = user.getStatus().name();

        this.passportNumber = user.getPassportNumber();
        this.passportIssuedDate = user.getPassportIssuedDate();
        this.passportExpiryDate = user.getPassportExpiryDate();
        this.passportCountry = user.getPassportCountry();
        this.englishFirstName = user.getEnglishFirstName();
        this.englishLastName = user.getEnglishLastName();

        this.emailVerified = user.getEmailVerified();
        this.marketingConsent = user.getMarketingConsent();
    }
}

