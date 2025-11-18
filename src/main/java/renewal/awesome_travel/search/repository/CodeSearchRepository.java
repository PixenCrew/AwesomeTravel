package renewal.awesome_travel.search.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import renewal.awesome_travel.search.dto.CodeSearchResponse;
import renewal.common.entity.CityCode;

@Repository
public interface CodeSearchRepository extends JpaRepository<CityCode, String> {

    @Query("""
            SELECT new renewal.awesome_travel.search.dto.CodeSearchResponse(
                        c.cityCode,
                        c.cityKor,
                        a.airportCode,
                        a.airportKor
                        )
                        FROM AirportCode a
                        JOIN a.cityCode c
                        """)
    List<CodeSearchResponse> loadAllCodeSearchItems();

}
