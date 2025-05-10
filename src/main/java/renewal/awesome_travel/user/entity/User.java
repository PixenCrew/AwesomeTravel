package renewal.awesome_travel.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import renewal.awesome_travel.comment.entity.Comment;
import renewal.awesome_travel.inquiry.entity.Inquiry;
import renewal.awesome_travel.member.utils.Provider;
import renewal.awesome_travel.member.utils.Role;
import renewal.awesome_travel.member.utils.Status;
import renewal.awesome_travel.purchase.entity.AirPurchase;
import renewal.awesome_travel.qna.entity.Qna;
import renewal.awesome_travel.wait.entity.Wait;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기본 로그인 정보
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password; // 소셜 로그인은 null 가능

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider; // LOCAL, GOOGLE, NAVER

    @Column(length = 100)
    private String providerId; // 소셜로그인 고유 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role; // USER, ADMIN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status; // ACTIVE, WITHDRAWN, BANNED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    // 여권 정보 (nullable)
    @Column(length = 20)
    private String passportNumber;

    private LocalDate passportIssuedDate;
    private LocalDate passportExpiryDate;

    @Column(length = 3)
    private String passportCountry; // 국가코드 (예: KOR)

    @Column(length = 50)
    private String englishFirstName;

    @Column(length = 50)
    private String englishLastName;

    private Boolean emailVerified;
    private Boolean marketingConsent;

//    @OneToMany(mappedBy = "user")
//    private List<Wait> waitList = new ArrayList<>();

//    @OneToMany(mappedBy = "user")
//    private List<Inquiry> inquiries = new ArrayList<>();
//
//    @OneToMany(mappedBy = "user")
//    private List<Qna> qnAS = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Comment> reviewComments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<AirPurchase> airPurchases = new ArrayList<>();

//    @OneToMany(mappedBy = "user")
//    private List<ProductPurchase> productPurchases = new ArrayList<>();


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = this.status == null ? Status.ACTIVE : this.status;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public void setPassportIssuedDate(LocalDate passportIssuedDate) {
        this.passportIssuedDate = passportIssuedDate;
    }

    public void setPassportExpiryDate(LocalDate passportExpiryDate) {
        this.passportExpiryDate = passportExpiryDate;
    }

    public void setPassportCountry(String passportCountry) {
        this.passportCountry = passportCountry;
    }

    public void setEnglishFirstName(String englishFirstName) {
        this.englishFirstName = englishFirstName;
    }

    public void setEnglishLastName(String englishLastName) {
        this.englishLastName = englishLastName;
    }

    public void setMarketingConsent(Boolean marketingConsent) {
        this.marketingConsent = marketingConsent;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

}
