package renewal.awesome_travel.air.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import renewal.awesome_travel.air.dto.response.SeatClassDetailResponseDto;
import renewal.awesome_travel.air.service.SeatClassService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seat-classes")
public class SeatClassController {

    private final SeatClassService seatClassService;

    @GetMapping("/{seatClassId}")
    public ResponseEntity<SeatClassDetailResponseDto> getSeatClassDetail(@PathVariable Long seatClassId) {
        return ResponseEntity.ok(seatClassService.getSeatClassDetail(seatClassId));
    }
}
