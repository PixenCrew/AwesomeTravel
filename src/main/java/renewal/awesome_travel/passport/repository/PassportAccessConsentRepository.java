package renewal.awesome_travel.passport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import renewal.awesome_travel.passport.entity.PassportAccessConsent;
import renewal.common.entity.User;

public interface PassportAccessConsentRepository extends JpaRepository<PassportAccessConsent, Long> {

    List<PassportAccessConsent> findByUser(User user);
}
