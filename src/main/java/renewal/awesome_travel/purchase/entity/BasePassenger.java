package renewal.awesome_travel.purchase.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.purchase.utiles.Sex;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@MappedSuperclass
public abstract class   BasePassenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //국내 국제 필수 입력값
    @Column(nullable = false)
    private String name; //풀네임 (한국인>한글, 외국인>영문)

    @Column(nullable = false)
    private String number; //전화번호

    @Column(nullable = false)
    private String email; //이메일

    @Column(nullable = false)
    private LocalDate birth; //생년월일

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Sex sex; //성별

    @ManyToOne
    @JoinColumn(name = "nationality_code", referencedColumnName = "countryCode", nullable = false)
    private Country nationality; //국적 REPUBLIC OF KOREA

    //국제선
    @Column(nullable = true)
    private String passport_num; //여권번호

    @Column(nullable = true)
    private String lastName; //영문 성

    @Column(nullable = true)
    private String firstName; //영문 이름

    @Column(nullable = true)
    private LocalDate expire; //만료일

    public BasePassenger(String name, String number, String email, LocalDate birth, Sex sex, Country nationality, String passport_num, String lastName, String firstName, LocalDate expire) {
        this.name = name;
        this.number = number;
        this.email = email;
        this.birth = birth;
        this.sex = sex;
        this.nationality = nationality;
        this.passport_num = passport_num;
        this.lastName = lastName;
        this.firstName = firstName;
        this.expire = expire;
    }

    public void setNationality(Country nationality) {
        this.nationality = nationality;
    }
}
