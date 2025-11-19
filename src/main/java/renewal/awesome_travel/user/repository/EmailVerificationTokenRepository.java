package renewal.awesome_travel.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import renewal.awesome_travel.user.entity.EmailVerificationToken;
import renewal.common.entity.User;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, String> {

    void deleteAllByUser(User user);
}
