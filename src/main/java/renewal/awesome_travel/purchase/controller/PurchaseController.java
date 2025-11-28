package renewal.awesome_travel.purchase.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.air.repository.AirRepository;
import renewal.awesome_travel.passport.dto.PassportUpdateRequest;
import renewal.common.entity.Air;
import renewal.common.entity.Airline;
import renewal.common.entity.PurchaseAir;
import renewal.common.entity.PurchaseBase.ConfirmedSeatClass;
import renewal.common.entity.PurchaseProduct;
import renewal.common.repository.PurchaseAirRepository;
import renewal.common.repository.PurchaseProductRepository;
import renewal.common.service.PassengerServiceCommon;

@Controller
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseAirRepository purchaseAirRepo;
    private final PurchaseProductRepository purchaseProductRepo;
    private final PassengerServiceCommon passengerServiceCommon;
    private final AirRepository airRepo;

    @GetMapping("/{type}/{id}/passport")
    public String getPassportForm(
            @PathVariable String type,
            @PathVariable Long id,
            Model model) {

        if (type.equals("air")) {
            PurchaseAir purchaseAir = purchaseAirRepo.findById(id).get();
            model.addAttribute("passengers", purchaseAir.getPassengers());
        } else if (type.equals("product")) {
            PurchaseProduct purchaseProduct = purchaseProductRepo.findById(id).get();
            model.addAttribute("passengers", purchaseProduct.getPassengers());
        } else {
            return "error";
        }
        model.addAttribute("type", type);
        model.addAttribute("purchaseBaseId", id);

        return "fragments/purchase/passengerForm";
    }

    @PostMapping("/{type}/{id}/passport")
    String postPurchasePassportForm(
            @PathVariable String type,
            @PathVariable Long id,
            @RequestBody PassportUpdateRequest request,
            Model model) {

        // PassportDto 리스트로부터 Passenger 업데이트 및 완료 여부 확인
        boolean allChecked = passengerServiceCommon.updatePassengersFromDto(request.getPassengers());

        if (type.equals("air")) {
            PurchaseAir purchaseAir = purchaseAirRepo.findById(id).orElseThrow();

            purchaseAir.setIsPassengerInfoComplete(allChecked);
            purchaseAirRepo.save(purchaseAir);
            
            // passengers를 다시 로드하여 completed 상태 반영
            purchaseAir = purchaseAirRepo.findById(id).orElseThrow();

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

        } else if (type.equals("product")) {
            PurchaseProduct purchaseProduct = purchaseProductRepo.findByIdWithAll(id).orElseThrow();

            purchaseProduct.setIsPassengerInfoComplete(allChecked);
            purchaseProductRepo.save(purchaseProduct);

            // passengers를 다시 로드하여 completed 상태 반영
            purchaseProduct = purchaseProductRepo.findByIdWithAll(id).orElseThrow();
            model.addAttribute("purchaseProduct", purchaseProduct);

            return "fragments/purchase/purchaseProductDetail";
        } else {
            return "error";
        }
    }

}
