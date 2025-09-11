package renewal.awesome_travel.passport.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.common.entity.PassengerBase.Sex;
import renewal.common.entity.CountryCode;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Passport")
public class Passport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "passport_id")
    private Long id;

    //국내 국제 필수 입력값
    @Column(nullable = false)
    private LocalDate birth; //생년월일

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Sex sex; //성별

    @ManyToOne
    @JoinColumn(name = "nationality_code", referencedColumnName = "code", nullable = false)
    private CountryCode nationality; //국적 REPUBLIC OF KOREA

    //국제선
    @Column(nullable = true)
    private String passportNum; //여권번호

    @Column(nullable = true)
    private String lastName; //영문 성

    @Column(nullable = true)
    private String firstName; //영문 이름

    @Column(nullable = true)
    private LocalDate expire; //만료일

    // @OneToOne(mappedBy = "passport")
    // private User user;

    public Passport(LocalDate birth, Sex sex, CountryCode nationality, String passportNum, String lastName, String firstName, LocalDate expire) {
        this.birth = birth;
        this.sex = sex;
        this.nationality = nationality;
        this.passportNum = passportNum;
        this.lastName = lastName;
        this.firstName = firstName;
        this.expire = expire;
    }

    // public void setUser(User user) {
    //     this.member = member;
    // }
}
