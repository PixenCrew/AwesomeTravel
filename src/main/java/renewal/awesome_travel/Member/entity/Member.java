package renewal.awesome_travel.member.entity;

import jakarta.persistence.*;
import lombok.*;
import renewal.awesome_travel.coupon.utiles.Grade;
import renewal.awesome_travel.passport.entity.Passport;
import renewal.awesome_travel.wait.entity.Wait;

import java.util.ArrayList;
import java.util.List;
// import renewal.awesome_travel.dto.TestDto;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
public class Member {
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String number;

    @Column
    private Integer point;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "passport_id", unique = true)
    private Passport passport;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Wait> waitList = new ArrayList<>();

    public Member(String email, String name, String number) {
        this.email = email;
        this.name = name;
        this.number = number;
        this.grade = Grade.GREEN;
        this.point = 0;
    }

    public void setPassport(Passport passport) {
        this.passport = passport;
        passport.setMember(this);
    }

    //qna, inquiry, review 등등 단방향 연결, 개별 조회필요
}
