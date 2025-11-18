package renewal.awesome_travel.user.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.awesome_travel.inquiry.repository.InquiryRepository;
import renewal.awesome_travel.passport.entity.Passport;
import renewal.awesome_travel.passport.repository.PassportAccessConsentRepository;
import renewal.awesome_travel.passport.repository.PassportRepository;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.common.entity.Inquiry;
import renewal.common.entity.PurchaseAir;
import renewal.common.entity.PurchaseProduct;
import renewal.common.entity.User;
import renewal.common.repository.PurchaseAirRepository;
import renewal.common.repository.PurchaseProductRepository;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final UserRepository userRepo;
    private final PurchaseProductRepository purchaseProductRepo;
    private final PurchaseAirRepository purchaseAirRepo;
    private final InquiryRepository inquiryRepo;
    private final PassportRepository passportRepo;
    private final PassportAccessConsentRepository passportAccessConsentRepo;

    @GetMapping("/reservation")
    public String mypageReservationFragment(@AuthenticationPrincipal CustomUserDetails principal, Model model) {

        User user = principal.getUser(); // detached 상태
        List<PurchaseProduct> purchaseProducts = purchaseProductRepo.findByUserId(user.getId());
        List<PurchaseAir> purchaseAirs = purchaseAirRepo.findByUserId(user.getId());

        model.addAttribute("purchaseProducts", purchaseProducts);
        model.addAttribute("purchaseAirs", purchaseAirs);

        // 로그인 되어 있으면 mypage fragment 반환
        return "fragments/purchase/purchaseList";
    }

    @GetMapping("/inquiry")
    public String mypageInquiryFragment(@AuthenticationPrincipal CustomUserDetails principal, Model model) {

        User user = principal.getUser(); // detached 상태
        List<Inquiry> inquiries = inquiryRepo.findByUserId(user.getId());

        model.addAttribute("inquiries", inquiries);

        // 로그인 되어 있으면 mypage fragment 반환
        return "fragments/inquiry/inquiryList";
    }

    @GetMapping("/setting")
    public String mypageSettingFragment(Principal principal, Model model) {

        User user = userRepo.findByEmail(principal.getName()).get();
        model.addAttribute("currentUser", user);

        return "fragments/mypage/setting";
    }

    @GetMapping("/myPassport")
    public String myPassportFragment(Principal principal, Model model) {

        User user = userRepo.findByEmail(principal.getName()).get();
        Passport myPassport = passportRepo.findByUser(user).orElseThrow();

        model.addAttribute("myPassport", myPassport);

        return "fragments/mypage/myPassport";
    }

    @PostMapping("/myPassport")
    public String myPassportPost(@RequestBody Passport myNewPassport, Principal principal) {

        User user = userRepo.findByEmail(principal.getName()).get();
        myNewPassport.setUser(user);
        passportRepo.save(myNewPassport);

        return "fragments/mypage";
    }

    @GetMapping("/mateList")
    public String mateListFragment(Principal principal, Model model) {

        return "fragments/mypage/mateList";
    }

}
