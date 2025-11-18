package renewal.awesome_travel.passport.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import renewal.awesome_travel.passport.entity.PassportAccessConsent;

public interface PassportAccessConsentRepository extends JpaRepository<PassportAccessConsent, Long> {
}
