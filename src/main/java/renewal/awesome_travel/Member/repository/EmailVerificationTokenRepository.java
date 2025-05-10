package renewal.awesome_travel.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.member.entity.EmailVerificationToken;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, String> {
}

