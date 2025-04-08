package renewal.awesome_travel.air.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import renewal.awesome_travel.air.dto.request.AirSearchRequestDto;
import renewal.awesome_travel.air.service.AirService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/air")
public class AirController {

    private final AirService airService;

    @PostMapping("/search")
    public ResponseEntity<Page<?>> searchFlights(@RequestBody AirSearchRequestDto req) {
        Page<?> result = airService.searchFlights(req);
        return ResponseEntity.ok(result);
    }
}
