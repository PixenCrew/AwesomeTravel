package renewal.awesome_travel.purchase.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import renewal.awesome_travel.purchase.dto.CountryDto;
import renewal.common.repository.CountryCodeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryCodeRepository countryCodeRepository;

    public List<CountryDto> getAllCountries() {
        return countryCodeRepository.findAll().stream()
                .map(c -> new CountryDto(
                        c.getCode(),
                        c.getEng(),
                        c.getKor()
                ))
                .toList();
    }
}
