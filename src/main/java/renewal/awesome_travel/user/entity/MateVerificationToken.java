package renewal.awesome_travel.user.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.passport.entity.PassportAccessConsent;
import renewal.common.entity.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MateVerificationToken {

    @Id
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 어떤 메이트(여권 동의)와 연결된 토큰인지
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passport_access_consent_id")
    private PassportAccessConsent matePassport;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public static MateVerificationToken create(User user, PassportAccessConsent matePassport) {
        MateVerificationToken evt = new MateVerificationToken();
        evt.token = UUID.randomUUID().toString();
        evt.user = user;
        evt.matePassport = matePassport;
        evt.createdAt = LocalDateTime.now();
        evt.expiresAt = evt.createdAt.plusHours(24);
        return evt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
