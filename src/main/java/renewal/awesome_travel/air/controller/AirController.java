package renewal.awesome_travel.air.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.air.dto.AirDetailRequestDto;
import renewal.awesome_travel.air.dto.AirSearchRequestDto;
import renewal.awesome_travel.air.dto.AirSearchResponseDto;
import renewal.awesome_travel.air.repository.SeatClassRepository;
import renewal.awesome_travel.air.service.AirService;
import renewal.common.entity.SeatClass;

@Controller
@RequiredArgsConstructor
@RequestMapping("/air")
public class AirController {

    private final AirService airService;
    private final SeatClassRepository seatClassRepo;

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
    public String showPurchasePage(@RequestBody AirDetailRequestDto detailRequest, Model model) {

        List<SeatClass> seatClasses = seatClassRepo.findAllWithAirInfoByIds(detailRequest.getSeatClassIds());
        seatClasses.sort(Comparator.comparing(sc -> sc.getAir().getDepartDateTime()));

        long PRICEADULT = 0L;
        long PRICEYOUTH = 0L;
        long PRICEINFANT = 0L;

        for (SeatClass seat : seatClasses) {
            PRICEADULT += seat.getPriceAdult();
            PRICEYOUTH += seat.getPriceYouth();
            PRICEINFANT += seat.getPriceInfant();
        }

        // ===============================
        // 정확한 비율 기반 분배
        // ===============================
        final double OIL_RATE = 0.25;
        final double TAX_RATE = 0.12;
        final long FEE = 5000L;

        int a = detailRequest.getAdultCount();
        int y = detailRequest.getYouthCount();
        int i = detailRequest.getInfantCount();

        // ---------- 성인 ----------
        long perAdultOil = Math.round(PRICEADULT * OIL_RATE);
        long perAdultTax = Math.round(PRICEADULT * TAX_RATE);
        long perAdultFee = FEE;
        long perAdultBase = PRICEADULT - perAdultOil - perAdultTax - perAdultFee;

        long adultOil = perAdultOil * a;
        long adultTax = perAdultTax * a;
        long adultFee = perAdultFee * a;
        long adultBase = perAdultBase * a;
        long adultTotal = PRICEADULT * a;

        // ---------- 청소년 ----------
        long perYouthOil = Math.round(PRICEYOUTH * OIL_RATE);
        long perYouthTax = Math.round(PRICEYOUTH * TAX_RATE);
        long perYouthFee = FEE;
        long perYouthBase = PRICEYOUTH - perYouthOil - perYouthTax - perYouthFee;

        long youthOil = perYouthOil * y;
        long youthTax = perYouthTax * y;
        long youthFee = perYouthFee * y;
        long youthBase = perYouthBase * y;
        long youthTotal = PRICEYOUTH * y;

        // ---------- 영유아 ----------
        long perInfantOil = Math.round(PRICEINFANT * OIL_RATE);
        long perInfantTax = Math.round(PRICEINFANT * TAX_RATE);
        long perInfantFee = FEE;
        long perInfantBase = PRICEINFANT - perInfantOil - perInfantTax - perInfantFee;

        long infantOil = perInfantOil * i;
        long infantTax = perInfantTax * i;
        long infantFee = perInfantFee * i;
        long infantBase = perInfantBase * i;
        long infantTotal = PRICEINFANT * i;

        long finalTotal = adultTotal + youthTotal + infantTotal;

        // ===============================
        // 모델 전달
        // ===============================

        model.addAttribute("detailRequest", detailRequest);
        model.addAttribute("seatClasses", seatClasses);

        model.addAttribute("priceAdult", PRICEADULT);
        model.addAttribute("priceYouth", PRICEYOUTH);
        model.addAttribute("priceInfant", PRICEINFANT);

        model.addAttribute("adultBase", adultBase);
        model.addAttribute("adultOil", adultOil);
        model.addAttribute("adultTax", adultTax);
        model.addAttribute("adultFee", adultFee);
        model.addAttribute("adultTotal", adultTotal);

        model.addAttribute("youthBase", youthBase);
        model.addAttribute("youthOil", youthOil);
        model.addAttribute("youthTax", youthTax);
        model.addAttribute("youthFee", youthFee);
        model.addAttribute("youthTotal", youthTotal);

        model.addAttribute("infantBase", infantBase);
        model.addAttribute("infantOil", infantOil);
        model.addAttribute("infantTax", infantTax);
        model.addAttribute("infantFee", infantFee);
        model.addAttribute("infantTotal", infantTotal);

        model.addAttribute("priceTotal", finalTotal);

        return "fragments/air/airDetail";
    }

    // @PostMapping("/search")
    // public ResponseEntity<Page<?>> searchFlights(@RequestBody AirSearchRequestDto
    // req) {
    // Page<?> result = airService.searchFlights(req);
    // return ResponseEntity.ok(result);
    // }

}
