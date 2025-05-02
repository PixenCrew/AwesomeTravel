package renewal.awesome_travel.purchase.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import renewal.awesome_travel.purchase.dto.CountryDto;
import renewal.awesome_travel.purchase.repository.CountryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    public List<CountryDto> getAllCountries() {
        return countryRepository.findAll().stream()
                .map(c -> new CountryDto(
                        c.getCountryCode(),
                        c.getCountryName(),
                        c.getCountryNameLocal()
                ))
                .toList();
    }
}
