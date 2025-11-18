package renewal.awesome_travel.user.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.awesome_travel.inquiry.repository.InquiryRepository;
import renewal.awesome_travel.passport.entity.Passport;
import renewal.awesome_travel.passport.repository.PassportAccessConsentRepository;
import renewal.awesome_travel.passport.repository.PassportRepository;
import renewal.awesome_travel.user.dto.request.UserRegisterRequestDto;
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
    private final PasswordEncoder passwordEncoder;

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

    @GetMapping("/userInfo")
    public String userInfoFragment(Principal principal, HttpSession session, Model model) {

        // 임시 새 사용자 DTO 생성
        UserRegisterRequestDto tempUser = new UserRegisterRequestDto();
        session.setAttribute("tempUser", tempUser);

        User user = userRepo.findByEmail(principal.getName()).orElseThrow();

        model.addAttribute("provider", user.getProvider());
        model.addAttribute("name", user.getName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("number", user.getPhone());

        return "fragments/mypage/userInfo";
    }

    @PostMapping("/userInfo/phone")
    public ResponseEntity<?> verifyNumber(@RequestBody Map<String, String> payload, HttpSession session) {

        UserRegisterRequestDto tempUser = (UserRegisterRequestDto) session.getAttribute("tempUser");
        String randomCode = String.format("%06d", new Random().nextInt(1000000));
        tempUser.setNumber(payload.get("number"));
        tempUser.setVerifyCode(randomCode);

        // =============== [TEST] 랜덤코드 문자로 보내는 기능 대체 ===================
        System.out.println("=============== [TEST] 랜덤코드 문자로 보내는 기능 대체 ===================");
        System.out.println("전화번호 : " + tempUser.getNumber());
        System.out.println("인증번호 : " + tempUser.getVerifyCode());
        System.out.println("=============== [TEST] 랜덤코드 문자로 보내는 기능 대체 ===================");

        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/userInfo/phone/check")
    public ResponseEntity<?> verifyNumberCheck(@RequestBody Map<String, String> payload, HttpSession session) {

        UserRegisterRequestDto tempUser = (UserRegisterRequestDto) session.getAttribute("tempUser");
        String tempUserNumber = tempUser.getNumber();
        String tempUserVerifyCode = tempUser.getVerifyCode();

        String requestNumber = payload.get("number");
        String requestVerifyCode = payload.get("verifyCode");

        if (tempUserNumber.equals(requestNumber) && tempUserVerifyCode.equals(requestVerifyCode)) {
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "인증번호가 올바르지 않습니다."));
        }

    }

    @PostMapping("/userInfo")
    public ResponseEntity<?> register(
            @RequestBody Map<String, String> payload,
            HttpSession session,
            Principal principal) {

        UserRegisterRequestDto tempUser = (UserRegisterRequestDto) session.getAttribute("tempUser");

        // verifyCode 코드 맞는지 확인
        String number = payload.get("number");
        String verifyCode = payload.get("verifyCode");

        if (!number.equals(tempUser.getNumber()) || !verifyCode.equals(tempUser.getVerifyCode())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "휴대폰 인증 정보가 올바르지 않습니다."));
        }

        // 회원정보 수정 진행
        String name = principal.getName();
        User user = userRepo.findByEmail(name).orElseThrow();
        user.setName(payload.get("name"));
        user.setPhone(payload.get("number"));
        userRepo.save(user);

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/userInfo/password")
    public String passwordFragment() {

        return "fragments/mypage/password";
    }

    @PostMapping("/userInfo/password")
    public ResponseEntity<?> passwordChange(@RequestBody Map<String, String> payload, Principal principal) {

        if (principal == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        // 회원정보 수정 진행
        String name = principal.getName();
        User user = userRepo.findByEmail(name).orElseThrow();

        String originalPassword = payload.get("originalPassword");
        String newPassword = payload.get("newPassword");
        String newPassword2 = payload.get("newPassword2");

        if (originalPassword == null || originalPassword.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "현재 비밀번호를 입력해주세요."));

        } else if (!passwordEncoder.matches(originalPassword, user.getPassword())) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "기존 비밀번호가 맞지 않습니다."));

        } else if (!newPassword.equals(newPassword2)) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "새 비밀번호가 일치하지 않습니다."));

        } else {

            // 패스워드 규칙 체크
            if (newPassword.length() < 8 || newPassword.length() > 32) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "비밀번호는 8자 이상 32자 이하로 설정해야 합니다."));
            }

            boolean hasLower = newPassword.chars().anyMatch(Character::isLowerCase);
            boolean hasDigit = newPassword.chars().anyMatch(Character::isDigit);
            boolean hasSpecial = newPassword.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

            int typeCount = 0;
            if (hasLower)
                typeCount++;
            if (hasDigit)
                typeCount++;
            if (hasSpecial)
                typeCount++;

            if (typeCount < 2) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "영문 소문자, 숫자, 특수문자 중 2가지 이상 조합해야 합니다."));
            }

            // 최종 패스워드 변경 진행
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepo.save(user);
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

}
