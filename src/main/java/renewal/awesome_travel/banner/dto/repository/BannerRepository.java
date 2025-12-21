package renewal.awesome_travel.banner.dto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import renewal.common.entity.Banner;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    
    // 활성화된 배너를 display_order 순서대로 조회 (기존 - 홈페이지용)
    List<Banner> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDisplayOrderAsc(
            LocalDate startDate, LocalDate endDate);
    
    // 특정 위치 타입과 식별자로 배너 조회
    List<Banner> findByLocationTypeAndLocationIdentifierAndActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDisplayOrderAsc(
            Banner.BannerLocationType locationType, 
            String locationIdentifier,
            LocalDate startDate, 
            LocalDate endDate);
    
    // 특정 위치 타입으로 배너 조회 (식별자 없이)
    List<Banner> findByLocationTypeAndActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDisplayOrderAsc(
            Banner.BannerLocationType locationType,
            LocalDate startDate, 
            LocalDate endDate);
}

