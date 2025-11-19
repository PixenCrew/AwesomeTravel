package renewal.awesome_travel.passport.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.common.entity.CountryCode;
import renewal.common.entity.Passenger.Sex;
import renewal.common.entity.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Passport")
public class Passport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn
    private User user;

    // ======== 여권 내용 ================
    @ManyToOne
    private CountryCode countryCode = new CountryCode(); // 국적 REPUBLIC OF KOREA
    private String passportNum; // 여권번호
    private String lastName; // 영문 성
    private String firstName; // 영문 이름
    private String lastNameKor; // 국문 성
    private String firstNameKor; // 국문 이름
    private LocalDate birth; // 생년월일
    @Enumerated(EnumType.STRING)
    private Sex sex; // 성별

    private String nationality; // 국적
    private String authority; // 발급관청
    private LocalDate issue; // 발급일
    private LocalDate expire; // 만료일
    // ======== 여권 내용 ================

}
