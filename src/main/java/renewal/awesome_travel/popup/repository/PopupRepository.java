package renewal.awesome_travel.popup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import renewal.awesome_travel.popup.entity.Popup;

import java.time.LocalDate;
import java.util.List;

public interface PopupRepository extends JpaRepository<Popup, Long> {
    @Query("SELECT p FROM Popup p WHERE p.active = true AND p.start <= :today AND p.end >= :today ORDER BY p.display_order ASC")
    List<Popup> findActivePopupsByDate(LocalDate today);
}

