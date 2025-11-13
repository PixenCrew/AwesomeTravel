package renewal.awesome_travel.air.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import renewal.common.entity.AirportCode;
import renewal.common.entity.SeatClass;
import renewal.common.entity.SeatClass.SeatClassType;

public interface SeatClassRepository extends JpaRepository<SeatClass, Long> {
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT s FROM SeatClass s WHERE s.id = :id")
        Optional<SeatClass> findByIdWithLock(@Param("id") Long id);

        SeatClass findTop1ByAir_DepartDateTimeBetweenAndAir_DepartAirportAndAir_ArriveAirportAndClassTypeInOrderByPriceAdultAsc(
                        LocalDateTime startOfDay,
                        LocalDateTime endOfDay,
                        AirportCode departAirport,
                        AirportCode arriveAirport,
                        Set<SeatClassType> classTypes);

        // 너무 길어서 래핑
        default SeatClass findLowestPriceSeat(
                        LocalDateTime startOfDay,
                        LocalDateTime endOfDay,
                        AirportCode departAirport,
                        AirportCode arriveAirport,
                        Set<SeatClass.SeatClassType> classTypes) {
                return findTop1ByAir_DepartDateTimeBetweenAndAir_DepartAirportAndAir_ArriveAirportAndClassTypeInOrderByPriceAdultAsc(
                                startOfDay, endOfDay, departAirport, arriveAirport, classTypes);
        }

        @Query("""
                            SELECT DISTINCT sc FROM SeatClass sc
                            JOIN FETCH sc.air a
                            JOIN FETCH a.airline al
                            JOIN FETCH a.departAirport da
                            JOIN FETCH a.arriveAirport aa
                            LEFT JOIN FETCH a.flightSegments fs
                            WHERE sc.id IN :ids
                        """)
        List<SeatClass> findAllWithAirInfoByIds(@Param("ids") List<Long> ids);

}
