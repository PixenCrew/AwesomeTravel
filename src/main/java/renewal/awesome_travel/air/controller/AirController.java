package renewal.awesome_travel.air.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.air.dto.AirSearchRequestDto;
import renewal.awesome_travel.air.dto.AirSearchResponseDto;
import renewal.awesome_travel.air.service.AirService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/air")
public class AirController {

    private final AirService airService;

    @GetMapping("/search")
    public String getAirSearch(Model model) {
        model.addAttribute("searchRequest", new AirSearchRequestDto());
        return "fragments/air/airSearch.html";
    }

    @PostMapping("/search")
    public String postAirSearch(@RequestBody AirSearchRequestDto searchRequest, Model model) {

        List<AirSearchResponseDto> resultList = airService.searchAir(searchRequest);
        System.out.println("===============Result=================" + resultList.toString());
        model.addAttribute("airSearchRequestDto", searchRequest);
        model.addAttribute("searchResult", resultList);
        return "fragments/air/airResult";
    }

    // @PostMapping("/search")
    // public ResponseEntity<Page<?>> searchFlights(@RequestBody AirSearchRequestDto
    // req) {
    // Page<?> result = airService.searchFlights(req);
    // return ResponseEntity.ok(result);
    // }

}
