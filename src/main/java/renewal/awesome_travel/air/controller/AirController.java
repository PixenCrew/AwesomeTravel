package renewal.awesome_travel.air.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.air.dto.AirDetailRequestDto;
import renewal.awesome_travel.air.dto.AirDetailResponseDto;
import renewal.awesome_travel.air.dto.AirSearchRequestDto;
import renewal.awesome_travel.air.dto.AirSearchResponseDto;
import renewal.awesome_travel.air.repository.AirRepository;
import renewal.awesome_travel.air.service.AirService;
import renewal.awesome_travel.payment.dto.PaymentRequest;
import renewal.awesome_travel.payment.repository.PaymentRepository;
import renewal.awesome_travel.purchase.service.PurchaseAirService;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.common.entity.Air;
import renewal.common.entity.Airline;
import renewal.common.entity.PassengerAir;
import renewal.common.entity.Payment;
import renewal.common.entity.PurchaseAir;
import renewal.common.entity.PurchaseBase.ConfirmedSeatClass;
import renewal.common.entity.PurchaseBase.PurchaseStatus;
import renewal.common.entity.SeatClass;
import renewal.common.entity.User;
import renewal.common.repository.PurchaseAirRepository;
import renewal.common.repository.SeatClassRepository;
import renewal.common.service.AirServiceCommon;
import renewal.common.service.PassengerServiceCommon;

@Controller
@RequiredArgsConstructor
@RequestMapping("/air")
public class AirController {

    private final AirService airService;
    private final AirServiceCommon airServiceCommon;
    private final SeatClassRepository seatClassRepo;
    private final UserRepository userRepo;
    private final PurchaseAirRepository purchaseAirRepo;
    private final PaymentRepository paymentRepo;
    private final PassengerServiceCommon passengerServiceCommon;
    private final PurchaseAirService purchaseAirService;
    private final AirRepository airRepo;

    @GetMapping("/search")
    public String getAirSearch(Model model) {
        model.addAttribute("searchRequest", new AirSearchRequestDto());
        return "fragments/air/airSearch";
    }

    @PostMapping("/search")
    public String postAirSearch(@RequestBody AirSearchRequestDto searchRequest, Model model) {

        List<AirSearchResponseDto> resultList = airService.searchAir(searchRequest);

        // 결과 제외 대상 확인
        if (searchRequest.isChangeSearch() && searchRequest.getExcludeSeatClassId() != null) {

            Long exclude = searchRequest.getExcludeSeatClassId();

            for (int i = 0; i < resultList.size(); i++) {

                AirSearchResponseDto dto = resultList.get(i);

                // 현재 항공편의 seatClassId
                Long seatClassId = dto.getTripList().get(0).getSeatClassId();

                if (seatClassId.equals(exclude)) {
                    resultList.remove(i); // 해당 요소 제거
                    break; // 바로 종료
                }
            }
        }

        // System.out.println("===============Result=================" +
        // resultList.toString());
        model.addAttribute("airSearchRequestDto", searchRequest);
        model.addAttribute("searchResult", resultList);
        return "fragments/air/airResult";
    }

    @PostMapping("/detail")
    public String showPurchasePage(@RequestBody AirDetailRequestDto detailRequest, Model model, HttpSession session) {

        List<SeatClass> seatClasses = seatClassRepo.findAllWithAirInfoByIds(detailRequest.getSeatClassIds());
        seatClasses.sort(Comparator.comparing(sc -> sc.getAir().getDepartDateTime()));

        AirDetailResponseDto detailResult = airService.calculateAirDetail(detailRequest, seatClasses);

        model.addAttribute("detailResult", detailResult);
        session.setAttribute("detailResult", detailResult);

        return "fragments/air/airDetail";
    }

    @GetMapping("/purchase/payment")
    String getPurchasePayForm(Model model, Principal principal, HttpSession session) {
        if (principal == null) {
            return "redirect:/login";
        }

        AirDetailResponseDto detailResult = (AirDetailResponseDto) session.getAttribute("detailResult");
        if (detailResult == null) {
            return "redirect:/air/search";
        }

        User buyer = userRepo.findByEmail(principal.getName()).orElse(null);
        if (buyer == null) {
            return "redirect:/login";
        }

        PurchaseAir purchaseAir = purchaseAirService.from(detailResult, buyer);

        session.setAttribute("purchaseAir", purchaseAir);

        model.addAttribute("purchaseBase", purchaseAir);
        model.addAttribute("detailResult", detailResult);
        model.addAttribute("paymentType", "air");

        return "fragments/payment";
    }

    @PostMapping("/purchase/payment")
    ResponseEntity<?> postPurchasePayForm(
            @RequestBody PaymentRequest request,
            Principal principal,
            Model model,
            HttpSession session) {

        PurchaseAir purchaseAir = (PurchaseAir) session.getAttribute("purchaseAir");
        User buyer = userRepo.findByEmail(principal.getName()).get();

        // 빈 PassengerAir들 생성
        List<PassengerAir> blankPassengers = passengerServiceCommon.createBlankPassengersAir(
                purchaseAir.getAdultCount().intValue(),
                purchaseAir.getYouthCount().intValue(),
                purchaseAir.getInfantCount().intValue());

        purchaseAir.setPassengers(blankPassengers);

        // 구매 상태 업데이트
        purchaseAir.setPurchaseStatus(PurchaseStatus.PAID);
        purchaseAirRepo.save(purchaseAir);

        // 결제 엔티티 생성
        Payment payment = new Payment();
        payment.setUser(buyer);
        payment.setPurchaseAir(purchaseAir);
        payment.setPaymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod()));
        payment.setPrice(purchaseAir.getPrice());
        payment.setPurchaseStatus(Payment.PaymentStatus.PAID);
        payment.setPurchaseDate(LocalDateTime.now());

        paymentRepo.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("purchaseAirId", purchaseAir.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/purchase/{id}")
    String getPurchaseDetail(@PathVariable Long id, Principal principal, Model model) {

        PurchaseAir purchaseAir = purchaseAirRepo.findById(id).orElse(null);
        if (purchaseAir == null) {
            return "error/error";
        }

        // 본인의 예약인지 확인
        if (principal != null) {
            User user = userRepo.findByEmail(principal.getName()).orElse(null);
            if (user != null && !purchaseAir.getUser().getId().equals(user.getId())) {
                return "error/error"; // 다른 사용자의 예약 접근 시 에러
            }
        } else {
            return "error/error"; // 로그인하지 않은 사용자 접근 시 에러
        }

        // finalSeatClasses의 airId를 사용해서 Air 엔티티를 조회하고 airline 정보를 Map으로 제공
        Map<Long, Airline> airlineMap = new HashMap<>();
        if (purchaseAir.getFinalSeatClasses() != null) {
            for (ConfirmedSeatClass confirmedSeatClass : purchaseAir.getFinalSeatClasses()) {
                if (confirmedSeatClass.getAirId() != null && !airlineMap.containsKey(confirmedSeatClass.getAirId())) {
                    Air air = airRepo.findById(confirmedSeatClass.getAirId()).orElse(null);
                    if (air != null && air.getAirline() != null) {
                        airlineMap.put(confirmedSeatClass.getAirId(), air.getAirline());
                    }
                }
            }
        }

        model.addAttribute("purchaseAir", purchaseAir);
        model.addAttribute("airlineMap", airlineMap);

        return "fragments/purchase/purchaseAirDetail";
    }

    @PostMapping("/purchase/{id}/cancel")
    ResponseEntity<?> cancelPurchase(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Principal principal) {

        // 본인의 예약인지 확인
        if (principal != null) {
            User user = userRepo.findByEmail(principal.getName()).orElse(null);
            PurchaseAir purchaseAir = purchaseAirRepo.findById(id).orElse(null);
            if (user == null || purchaseAir == null || !purchaseAir.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "권한이 없습니다."));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        // 환불 금액과 사유 추출
        Long amount = request.get("amount") != null ? 
                Long.parseLong(request.get("amount").toString()) : null;
        String reason = request.get("reason") != null ? 
                request.get("reason").toString() : "환불 요청";

        if (amount == null) {
            // Payment에서 금액 조회
            Payment payment = paymentRepo.findByPurchaseAirId(id).orElse(null);
            if (payment != null) {
                amount = payment.getPrice();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "결제 정보를 찾을 수 없습니다."));
            }
        }

        // 환불 요청 생성 (바로 취소하지 않고 REQUESTED 상태로 생성)
        try {
            airServiceCommon.requestRefund(id, amount, reason);
            return ResponseEntity.ok(Map.of("success", true, "message", "환불 요청이 접수되었습니다. 관리자 승인 후 처리됩니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
