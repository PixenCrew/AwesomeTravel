package renewal.awesome_travel.purchase.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import renewal.awesome_travel.purchase.dto.SpecialRequestDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/special-requests")
public class SpecialRequestController {

    // private final SpecialRequestRepository specialRequestRepository;

    // // 사용자용 - 전체 특별요청 목록 조회 (체크박스용)
    // @GetMapping
    // public ResponseEntity<List<SpecialRequestDto>> getAllRequests() {
    //     List<SpecialRequestDto> result = specialRequestRepository.findAll().stream()
    //             .map(req -> new SpecialRequestDto(
    //                     req.getId(),
    //                     req.getRequestType(),
    //                     req.getDescription()
    //             ))
    //             .toList();
    //     return ResponseEntity.ok(result);
    // }
}

