package renewal.awesome_travel.purchase.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import renewal.awesome_travel.purchase.dto.CountryDto;
import renewal.awesome_travel.purchase.service.CountryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/countries")
public class CountryController {

    private final CountryService countryService;

    // 사용자용 - 전체 국가 목록 조회
    @GetMapping
    public ResponseEntity<List<CountryDto>> getAllCountries() {
        return ResponseEntity.ok(countryService.getAllCountries());
    }
}
