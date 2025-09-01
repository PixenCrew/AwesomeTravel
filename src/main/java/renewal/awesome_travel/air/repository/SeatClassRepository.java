package renewal.awesome_travel.air.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import renewal.common.entity.SeatClass;

import java.util.Optional;

public interface SeatClassRepository extends JpaRepository<SeatClass, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatClass s WHERE s.id = :id")
    Optional<SeatClass> findByIdWithLock(@Param("id") Long id);
}
