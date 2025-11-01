package renewal.awesome_travel.product.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import renewal.common.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // Tour → CountryCode 기준
    List<Product> findAllByTour_Country_CountryCodeIn(Collection<String> countryCodes);

    // Tour → Schedules → Location → CityCode 기준
    List<Product> findDistinctByTour_Schedules_Locations_CityCode_CityCodeIn(Collection<String> cityCodes);

}
