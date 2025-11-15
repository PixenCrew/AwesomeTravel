package renewal.awesome_travel.air.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import renewal.awesome_travel.air.repository.SeatClassRepository;
import renewal.awesome_travel.air.service.AirService;
import renewal.awesome_travel.passport.dto.request.PassportDto;
import renewal.awesome_travel.passport.dto.request.PassportUpdateRequest;
import renewal.awesome_travel.payment.dto.PaymentRequest;
import renewal.awesome_travel.payment.repository.PaymentRepository;
import renewal.awesome_travel.purchase.repository.PurchaseAirRepository;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.common.entity.Passenger;
import renewal.common.entity.Passenger.AgeGroup;
import renewal.common.entity.Payment;
import renewal.common.entity.PurchaseAir;
import renewal.common.entity.PurchaseBase.PurchaseStatus;
import renewal.common.entity.SeatClass;
import renewal.common.entity.User;
import renewal.common.repository.CountryCodeRepository;
import renewal.common.repository.PassengerRepository;

@Controller
@RequiredArgsConstructor
@RequestMapping("/air")
public class AirController {

    private final AirService airService;
    private final SeatClassRepository seatClassRepo;
    private final UserRepository userRepo;
    private final PurchaseAirRepository purchaseAirRepo;
    private final PaymentRepository paymentRepo;
    private final PassengerRepository passengerRepo;
    private final CountryCodeRepository countryCodeRepo;

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

        System.out.println("===============Result=================" + resultList.toString());
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

        AirDetailResponseDto detailResult = (AirDetailResponseDto) session.getAttribute("detailResult");
        User buyer = userRepo.findByEmail(principal.getName()).get();
        PurchaseAir purchaseAir = PurchaseAir.from(detailResult, buyer);

        session.setAttribute("purchaseAir", purchaseAir);

        model.addAttribute("purchaseBase", purchaseAir);
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

        // 빈 Passenger들 생성
        List<Passenger> blankPassengers = new ArrayList<>();

        for (int i = 0; i < purchaseAir.getAdultCount(); i++) {
            Passenger adultPassenger = new Passenger();
            adultPassenger.setAgeGroup(AgeGroup.ADULT);
            blankPassengers.add(adultPassenger);
        }
        for (int i = 0; i < purchaseAir.getAdultCount(); i++) {
            Passenger youthPassenger = new Passenger();
            youthPassenger.setAgeGroup(AgeGroup.YOUTH);
            blankPassengers.add(youthPassenger);
        }
        for (int i = 0; i < purchaseAir.getAdultCount(); i++) {
            Passenger infantPassenger = new Passenger();
            infantPassenger.setAgeGroup(AgeGroup.INFANT);
            blankPassengers.add(infantPassenger);
        }
        passengerRepo.saveAll(blankPassengers);
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
    String getPurchaseDetail(@PathVariable Long id, Model model) {

        // TODO Principal principal로 해당 구매id 조회 가능한 사용자인지 확인

        PurchaseAir purchaseAir = purchaseAirRepo.findById(id).get();

        model.addAttribute("purchaseAir", purchaseAir);
        model.addAttribute("paymentInfo", "");

        return "fragments/purchase/purchaseAirDetail";
    }

    @GetMapping("/purchase/{id}/passport")
    String getPurchasePassportForm(@PathVariable Long id, Model model) {

        PurchaseAir purchaseAir = purchaseAirRepo.findById(id).get();

        model.addAttribute("passengers", purchaseAir.getPassengers());
        model.addAttribute("purchaseAirId", id);

        return "fragments/purchase/passengerForm";
    }

    @PostMapping("/purchase/{id}/passport")
    String postPurchasePassportForm(@PathVariable Long id, @RequestBody PassportUpdateRequest request, Model model) {

        List<PassportDto> passengers = request.getPassengers();
        boolean allChecked = true;
        for (PassportDto dto : passengers) {
            // 기존 Passenger 조회
            Passenger passenger = passengerRepo.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("탑승객 ID가 유효하지 않습니다: " + dto.getId()));

            // 여권정보 업데이트
            passenger.setNationality(countryCodeRepo.findByCode(dto.getNationality()).get());
            passenger.setPassportNum(dto.getPassportNum());
            passenger.setLastName(dto.getLastName());
            passenger.setFirstName(dto.getFirstName());
            passenger.setExpire(dto.getExpire());
            passenger.setSpecialRequests(dto.getSpecialRequests());

            // 일반정보 업데이트
            passenger.setName(dto.getName());
            passenger.setBirth(dto.getBirth());
            passenger.setSex(dto.getSex());
            passenger.setNumber(dto.getNumber());
            passenger.setEmail(dto.getEmail());
            passenger.setAgeGroup(dto.getAgeGroup());

            // 해당 탑승객 정보 null 체크
            passenger.checkThisPassenger();
            if (passenger.isCompleted() == false) {
                allChecked = false;
            }

            passengerRepo.save(passenger);
        }

        PurchaseAir purchaseAir = purchaseAirRepo.findById(id).get();
        purchaseAir.setIsPassengerInfoComplete(allChecked);

        model.addAttribute("purchaseAir", purchaseAir);
        model.addAttribute("paymentInfo", "");

        return "fragments/purchase/purchaseAirDetail";
    }

    // @PostMapping("/search")
    // public ResponseEntity<Page<?>> searchFlights(@RequestBody AirSearchRequestDto
    // req) {
    // Page<?> result = airService.searchFlights(req);
    // return ResponseEntity.ok(result);
    // }

}
