package renewal.awesome_travel.purchase.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.passport.dto.PassportDto;
import renewal.awesome_travel.passport.dto.PassportUpdateRequest;
import renewal.common.entity.Passenger;
import renewal.common.entity.PurchaseAir;
import renewal.common.entity.PurchaseBase;
import renewal.common.entity.PurchaseProduct;
import renewal.common.repository.CountryCodeRepository;
import renewal.common.repository.PassengerRepository;
import renewal.common.repository.PurchaseAirRepository;
import renewal.common.repository.PurchaseProductRepository;

@Controller
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseAirRepository purchaseAirRepo;
    private final PurchaseProductRepository purchaseProductRepo;
    private final CountryCodeRepository countryCodeRepo;
    private final PassengerRepository passengerRepo;

    @GetMapping("/{type}/{id}/passport")
    public String getPassportForm(
            @PathVariable String type,
            @PathVariable Long id,
            Model model) {

        PurchaseBase purchaseBase;

        if (type.equals("air")) {
            purchaseBase = purchaseAirRepo.findById(id).get();
        } else if (type.equals("product")) {
            purchaseBase = purchaseProductRepo.findById(id).get();
        } else {
            return "error";
        }

        model.addAttribute("passengers", purchaseBase.getPassengers());
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

        List<PassportDto> passengers = request.getPassengers();
        boolean allChecked = true;
        for (PassportDto dto : passengers) {
            // 기존 Passenger 조회
            Passenger passenger = passengerRepo.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("탑승객 ID가 유효하지 않습니다: " + dto.getId()));

            // 여권정보 업데이트
            passenger.setCountryCode(countryCodeRepo.findByCode(dto.getCountryCode()).get());
            passenger.setPassportNum(dto.getPassportNum());
            passenger.setLastName(dto.getLastName());
            passenger.setFirstName(dto.getFirstName());
            passenger.setLastNameKor(dto.getLastNameKor());
            passenger.setFirstNameKor(dto.getFirstNameKor());
            passenger.setBirth(dto.getBirth());
            passenger.setSex(dto.getSex());

            passenger.setNationality(dto.getNationality());
            passenger.setAuthority(dto.getAuthority());
            passenger.setIssue(dto.getIssue());
            passenger.setExpire(dto.getExpire());

            // 일반정보 업데이트
            passenger.setNumber(dto.getNumber());
            passenger.setEmail(dto.getEmail());
            passenger.setSpecialRequests(dto.getSpecialRequests());
            passenger.setAgeGroup(dto.getAgeGroup());

            // 해당 탑승객 정보 null 체크
            passenger.checkThisPassenger();
            if (passenger.isCompleted() == false) {
                allChecked = false;
            }

            passengerRepo.save(passenger);
        }

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
