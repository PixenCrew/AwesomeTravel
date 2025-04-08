package renewal.awesome_travel.air.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.air.entity.Airline;

import java.util.Optional;

public interface AirlineRepository extends JpaRepository<Airline,String> {
    // 항공사명(한글) 기준 조회
    Optional<Airline> findByNameKor(String nameKor);

    // 항공사명(영문) 기준 조회
    Optional<Airline> findByNameEng(String nameEng);

    // 항공사 코드 기준 조회 (예: "KE", "OZ" 등)
    Optional<Airline> findByCode(String code);
}
