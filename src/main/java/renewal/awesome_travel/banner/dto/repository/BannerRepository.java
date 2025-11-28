package renewal.awesome_travel.banner.dto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import renewal.common.entity.Banner;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    
    // 활성화된 배너를 display_order 순서대로 조회
    List<Banner> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDisplayOrderAsc(
            LocalDate startDate, LocalDate endDate);
}

