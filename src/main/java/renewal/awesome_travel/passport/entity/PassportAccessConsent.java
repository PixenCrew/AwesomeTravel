package renewal.awesome_travel.passport.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import renewal.common.entity.User;

@Entity
@Getter
@Setter
public class PassportAccessConsent {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 접근하고자 하는 여권
    @OneToOne
    private Passport passport;

    // SMS 링크에 포함될 승인 토큰
    @Column(unique = true)
    private String approvalToken;
    private String ownerName;
    private String ownerPhone;

    @Enumerated(EnumType.STRING)
    private ConsentStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    public enum ConsentStatus {
        PENDING,
        ACCEPTED,
        EXPIRED
    }
}
