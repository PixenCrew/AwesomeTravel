package renewal.awesome_travel.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.purchase.entity.Country;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, String> {

    // 국가 코드(countryCode)로 조회
    Optional<Country> findByCountryCode(String countryCode);

    // 국가 영문명(countryName)으로 조회
    Optional<Country> findByCountryName(String countryName);
}
