package renewal.awesome_travel.purchase.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/air-purchases")
@RequiredArgsConstructor
public class PurchaseAirController {

    // private final PurchaseAirService airPurchaseService;

    // // 1. 좌석 홀딩 (예약만)
    // @PostMapping
    // public ResponseEntity<Long> holdSeats(@RequestBody PurchaseAirRequestDto
    // request) {
    // Long purchaseId = airPurchaseService.holdSeats(request);
    // return ResponseEntity.ok(purchaseId);
    // }

    // // 2. 결제 성공 후 확정 처리
    // @PatchMapping("/{id}/confirm")
    // public ResponseEntity<Void> confirmPayment(@PathVariable Long id) {
    // airPurchaseService.confirmPayment(id);
    // return ResponseEntity.ok().build();
    // }

    // // 3. 구매 상세 조회
    // @GetMapping("/{id}")
    // public ResponseEntity<PurchaseAirResponseDto> getPurchase(@PathVariable Long
    // id) {
    // return ResponseEntity.ok(airPurchaseService.getPurchase(id));
    // }

    // //4. 예약 취소
    // @PatchMapping("/{id}/cancel")
    // public ResponseEntity<Void> cancelPurchase(@PathVariable Long id) {
    // airPurchaseService.cancelPurchase(id);
    // return ResponseEntity.ok().build();
    // }

    // //5. 탑승객 정보 변경
    // @PatchMapping("/{purchaseId}/passengers/{passengerId}")
    // public ResponseEntity<Void> updatePassenger(
    // @PathVariable Long purchaseId,
    // @PathVariable Long passengerId,
    // @RequestBody PassengerUpdateRequestDto dto) {
    // airPurchaseService.updatePassenger(purchaseId, passengerId, dto);
    // return ResponseEntity.ok().build();
    // }

}
