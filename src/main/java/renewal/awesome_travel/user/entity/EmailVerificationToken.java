package renewal.awesome_travel.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class EmailVerificationToken {

    @Id
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public static EmailVerificationToken create(User user) {
        EmailVerificationToken evt = new EmailVerificationToken();
        evt.token = UUID.randomUUID().toString();
        evt.user = user;
        evt.createdAt = LocalDateTime.now();
        evt.expiresAt = evt.createdAt.plusHours(24); // 유효기간 24시간
        return evt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

