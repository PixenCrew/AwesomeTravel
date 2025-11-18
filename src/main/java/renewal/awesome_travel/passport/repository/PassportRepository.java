package renewal.awesome_travel.passport.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import renewal.awesome_travel.passport.entity.Passport;
import renewal.common.entity.User;

public interface PassportRepository extends JpaRepository<Passport, Long> {

    Optional<Passport> findByUser(User user);
}
