package renewal.awesome_travel.popup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import renewal.common.entity.Popup;

import java.time.LocalDate;
import java.util.List;

public interface PopupRepository extends JpaRepository<Popup, Long> {
    @Query("SELECT p FROM Popup p WHERE p.active = true AND p.startDate <= :today AND p.endDate >= :today ORDER BY p.displayOrder ASC")
    List<Popup> findActivePopupsByDate(LocalDate today);
}

