package renewal.awesome_travel.purchase.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.passport.dto.PassportUpdateRequest;
import renewal.common.entity.PurchaseAir;
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
            PurchaseAir purchaseAir = purchaseAirRepo.findById(id).get();

            purchaseAir.setIsPassengerInfoComplete(allChecked);
            purchaseAirRepo.save(purchaseAir);

            model.addAttribute("purchaseAir", purchaseAir);

            return "fragments/purchase/purchaseAirDetail";

        } else if (type.equals("product")) {
            PurchaseProduct purchaseProduct = purchaseProductRepo.findById(id).get();

            purchaseProduct.setIsPassengerInfoComplete(allChecked);
            purchaseProductRepo.save(purchaseProduct);

            model.addAttribute("purchaseProduct", purchaseProduct);

            return "fragments/purchase/purchaseProductDetail";
        } else {
            return "error";
        }
    }

}
