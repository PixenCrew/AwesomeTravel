package renewal.awesome_travel.air.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import renewal.awesome_travel.air.dto.AirSearchRequestDto;
import renewal.awesome_travel.air.service.AirService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/air")
public class AirController {

    private final AirService airService;

    @GetMapping("/search")
    public String getAirSearch(Model model) {
        model.addAttribute("searchRequest", new AirSearchRequestDto());
        return "air/airSearch";
    }

    @PostMapping("/search")
    public String postAirSearch(
            @ModelAttribute AirSearchRequestDto searchRequest,
            Model model) {
                System.out.println(searchRequest.getAdultCount());
        model.addAttribute("searchRequest", new AirSearchRequestDto());
        return "air/airSearch";
    }

    // @PostMapping("/search")
    // public ResponseEntity<Page<?>> searchFlights(@RequestBody AirSearchRequestDto
    // req) {
    // Page<?> result = airService.searchFlights(req);
    // return ResponseEntity.ok(result);
    // }

}
