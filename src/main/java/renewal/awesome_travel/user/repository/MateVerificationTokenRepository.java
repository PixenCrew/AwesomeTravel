package renewal.awesome_travel.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import renewal.awesome_travel.passport.entity.PassportAccessConsent;
import renewal.awesome_travel.user.entity.MateVerificationToken;

public interface MateVerificationTokenRepository extends JpaRepository<MateVerificationToken, String> {

    Optional<MateVerificationToken> findByMatePassport(PassportAccessConsent matePassport);
}
