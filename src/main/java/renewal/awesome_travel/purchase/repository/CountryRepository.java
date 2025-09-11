package renewal.awesome_travel.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import renewal.common.entity.CountryCode;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<CountryCode, String> {

    // 국가 코드(countryCode)로 조회
    Optional<CountryCode> findByCode(String countryCode);

    // 국가 영문명(countryName)으로 조회
    Optional<CountryCode> findByEng(String countryName);
}
