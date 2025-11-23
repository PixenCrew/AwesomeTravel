package renewal.awesome_travel.user.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.awesome_travel.inquiry.repository.InquiryRepository;
import renewal.awesome_travel.passport.entity.PassportAccessConsent;
import renewal.awesome_travel.passport.repository.PassportAccessConsentRepository;
import renewal.awesome_travel.passport.repository.PassportRepository;
import renewal.awesome_travel.payment.repository.PaymentRepository;
import renewal.awesome_travel.user.dto.MemberGradeStatsDto;
import renewal.awesome_travel.user.dto.request.MatePassportRequestDto;
import renewal.awesome_travel.user.dto.request.PassportRequestDto;
import renewal.awesome_travel.user.dto.request.UserRegisterRequestDto;
import renewal.awesome_travel.user.entity.MateVerificationToken;
import renewal.awesome_travel.user.repository.MateVerificationTokenRepository;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.awesome_travel.user.service.UserService;
import renewal.common.entity.CountryCode;
import renewal.common.entity.Inquiry;
import renewal.common.entity.Passenger.Sex;
import renewal.common.entity.Passport;
import renewal.common.entity.PurchaseAir;
import renewal.common.entity.PurchaseProduct;
import renewal.common.entity.User;
import renewal.common.entity.User.UserProvider;
import renewal.common.entity.User.UserStatus;
import renewal.common.repository.CountryCodeRepository;
import renewal.common.repository.PurchaseAirRepository;
import renewal.common.repository.PurchaseProductRepository;
import renewal.common.service.EmailService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final UserRepository userRepo;
    private final UserService userService;
    private final PurchaseProductRepository purchaseProductRepo;
    private final PurchaseAirRepository purchaseAirRepo;
    private final PaymentRepository paymentRepo;
    private final CountryCodeRepository countryCodeRepo;
    private final InquiryRepository inquiryRepo;
    private final PassportRepository passportRepo;
    private final PassportAccessConsentRepository passportAccessConsentRepo;
    private final PasswordEncoder passwordEncoder;
    private final MateVerificationTokenRepository mateTokenRepository;
    private final EmailService emailService;

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
        Passport myPassport = passportRepo.findByUser(user).orElseGet(Passport::new);

        model.addAttribute("myPassport", myPassport);

        return "fragments/mypage/myPassport";
    }

    @PostMapping("/myPassport")
    public ResponseEntity<?> myPassportPost(@RequestBody PassportRequestDto myPassport, Principal principal) {

        User user = userRepo.findByEmail(principal.getName()).get();

        Passport passport = passportRepo.findByUser(user)
                .orElse(new Passport());

        // 새로 생성된 passport라면 user 연결
        if (passport.getId() == null) {
            passport.setUser(user);
        }

        // CountryCode 엔티티 변환
        CountryCode countryCode = countryCodeRepo.findById(myPassport.getCountryCode())
                .orElseThrow(() -> new IllegalArgumentException("국가코드 없음"));

        passport.setCountryCode(countryCode);
        passport.setPassportNum(myPassport.getPassportNum());
        passport.setLastName(myPassport.getLastName());
        passport.setFirstName(myPassport.getFirstName());
        passport.setLastNameKor(myPassport.getLastNameKor());
        passport.setFirstNameKor(myPassport.getFirstNameKor());

        passport.setBirth(LocalDate.parse(myPassport.getBirth()));
        passport.setSex(Sex.valueOf(myPassport.getSex()));

        passport.setNationality(myPassport.getNationality());
        passport.setAuthority(myPassport.getAuthority());

        passport.setIssue(LocalDate.parse(myPassport.getIssue()));
        passport.setExpire(LocalDate.parse(myPassport.getExpire()));

        passportRepo.save(passport);

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/mateList")
    public String mateListFragment(Principal principal, Model model) {

        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        List<PassportAccessConsent> mateList = passportAccessConsentRepo.findByUser(user);

        model.addAttribute("mateList", mateList);

        return "fragments/mypage/mateList";
    }

    // 여권입력시 조회용
    @GetMapping("/mateList/select")
    public String mateListSelect(Principal principal, Model model) {

        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        Passport myPassport = passportRepo.findByUser(user).orElse(null);
        if (myPassport != null) {
            model.addAttribute("myPassport", myPassport);
        }

        List<PassportAccessConsent> mateList = passportAccessConsentRepo.findByUser(user);

        model.addAttribute("mateList", mateList);

        return "fragments/mypage/mateListSelect";
    }

    @GetMapping("/api/myPassport")
    public ResponseEntity<?> getMyPassport(Principal principal) {
        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        Passport passport = passportRepo.findByUser(user).orElseThrow();

        Map<String, Object> dto = new HashMap<>();
        dto.put("passportNum", passport.getPassportNum());
        dto.put("lastName", passport.getLastName());
        dto.put("firstName", passport.getFirstName());
        dto.put("lastNameKor", passport.getLastNameKor());
        dto.put("firstNameKor", passport.getFirstNameKor());
        dto.put("birth", passport.getBirth().toString());
        dto.put("sex", passport.getSex().name());
        dto.put("countryCode", passport.getCountryCode().getCode());
        dto.put("nationality", passport.getNationality());
        dto.put("authority", passport.getAuthority());
        dto.put("issue", passport.getIssue().toString());
        dto.put("expire", passport.getExpire().toString());
        dto.put("email", user.getEmail());
        dto.put("number", user.getPhone());

        return ResponseEntity.ok(dto);
    }

    // 여권입력시 특정 여행메이트 정보 API
    @GetMapping("/api/matePassport/{id}")
    public ResponseEntity<?> getMateInfo(@PathVariable Long id) {

        PassportAccessConsent mate = passportAccessConsentRepo.findById(id).orElseThrow();

        Passport p = mate.getPassport();

        Map<String, Object> dto = new HashMap<>();
        dto.put("passportNum", p.getPassportNum());
        dto.put("lastName", p.getLastName());
        dto.put("firstName", p.getFirstName());
        dto.put("lastNameKor", p.getLastNameKor());
        dto.put("firstNameKor", p.getFirstNameKor());
        dto.put("birth", p.getBirth().toString());
        dto.put("sex", p.getSex().name());
        dto.put("countryCode", p.getCountryCode().getCode());
        dto.put("nationality", p.getNationality());
        dto.put("authority", p.getAuthority());
        dto.put("issue", p.getIssue().toString());
        dto.put("expire", p.getExpire().toString());
        dto.put("email", mate.getEmail());
        dto.put("number", mate.getNumber());

        return ResponseEntity.ok(dto);
    }

    // 새 메이트 폼
    @GetMapping("/matePassport/new")
    public String newMateFragment(Principal principal, Model model) {

        PassportAccessConsent matePassport = new PassportAccessConsent();
        model.addAttribute("matePassport", matePassport);

        return "fragments/mypage/matePassport";
    }

    // 특정 메이트 조회
    @GetMapping("/matePassport/{id}")
    public String getMateFragment(@PathVariable Long id, Principal principal, Model model) {

        PassportAccessConsent matePassport = passportAccessConsentRepo.findById(id).orElseThrow();
        model.addAttribute("matePassport", matePassport);

        return "fragments/mypage/matePassport";
    }

    // 메이트 등록 ( 신규 / 수정 )
    @PostMapping("/matePassport")
    public ResponseEntity<?> saveMatePassport(@RequestBody MatePassportRequestDto req,
            Principal principal) {

        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow();

        PassportAccessConsent matePassport = (req.getId() != null)
                ? passportAccessConsentRepo.findById(req.getId()).orElse(new PassportAccessConsent())
                : new PassportAccessConsent();

        matePassport.setUser(user);
        matePassport.setEmail(req.getEmail());
        matePassport.setNumber(req.getNumber());
        matePassport.getPassport().setPassportNum(req.getPassportNum());
        matePassport.getPassport().setLastName(req.getLastName());
        matePassport.getPassport().setFirstName(req.getFirstName());
        matePassport.getPassport().setLastNameKor(req.getLastNameKor());
        matePassport.getPassport().setFirstNameKor(req.getFirstNameKor());
        matePassport.getPassport().setBirth(LocalDate.parse(req.getBirth()));
        matePassport.getPassport().setSex(Sex.valueOf(req.getSex()));
        matePassport.getPassport().setNationality(req.getNationality());
        matePassport.getPassport().setAuthority(req.getAuthority());
        matePassport.getPassport().setIssue(LocalDate.parse(req.getIssue()));
        matePassport.getPassport().setExpire(LocalDate.parse(req.getExpire()));

        // 국가코드
        CountryCode code = countryCodeRepo.findByCode(req.getCountryCode())
                .orElseThrow();
        matePassport.getPassport().setCountryCode(code);

        passportRepo.save(matePassport.getPassport());
        passportAccessConsentRepo.save(matePassport);

        // 새 등록이면 이메일 발송
        if (req.getId() == null) {
            MateVerificationToken token = MateVerificationToken.create(user, matePassport);
            matePassport.setRequestedAt(LocalDateTime.now());
            mateTokenRepository.save(token);
            emailService.sendMateMail(req.getEmail(), token.getToken(), "/mypage/matePassport/accept?token="); // 비동기
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    // 특정 메이트 승낙
    @GetMapping("/matePassport/accept")
    public String acceptMateFragment(@RequestParam String token, Model model) {

        MateVerificationToken token2 = mateTokenRepository.findById(token).orElseThrow();

        PassportAccessConsent matePassport = passportAccessConsentRepo.findById(token2.getMatePassport().getId())
                .orElseThrow();
        matePassport.setApprovedAt(LocalDateTime.now());
        passportAccessConsentRepo.save(matePassport);
        mateTokenRepository.delete(token2);
        model.addAttribute("username", matePassport.getUser().getName());

        return "fragments/mypage/matePassportAccepted";
    }

    // 특정 메이트 삭제
    @PostMapping("/matePassport/{id}/delete")
    public ResponseEntity<?> deleteMate(@PathVariable @NonNull Long id) {
        PassportAccessConsent matePassport = passportAccessConsentRepo.findById(id).get();
        if (matePassport != null) {
            Passport passport = matePassport.getPassport();
            MateVerificationToken token = mateTokenRepository.findByMatePassport(matePassport).orElse(null);

            // token 있으면 삭제
            if (token != null) {
                mateTokenRepository.delete(token);
            }

            // mate 삭제
            passportAccessConsentRepo.delete(matePassport);

            // passport 삭제
            if (passport != null) {
                passportRepo.delete(passport);
            }
            return ResponseEntity.ok(Map.of("success", true));

        }
        return ResponseEntity.ok(Map.of("success", false));

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

    @GetMapping("/withdrawal")
    public String withdrawalFragment(Principal principal) {

        // 회원정보 수정 진행
        String name = principal.getName();
        User user = userRepo.findByEmail(name).orElseThrow();

        if (user.getProvider() == UserProvider.LOCAL) {
            return "fragments/mypage/withdrawal";
        } else {
            return "fragments/mypage/withdrawal2";
        }
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<?> withdrawalUser(@RequestBody Map<String, String> payload, Principal principal) {

        if (principal == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        // 회원정보 수정 진행
        String name = principal.getName();
        User user = userRepo.findByEmail(name).orElseThrow();

        // 1. 소셜로그인이면 비밀번호 체크 없이 진행
        if (user.getProvider() != UserProvider.LOCAL) {
            user.setStatus(UserStatus.WITHDRAWN);
            userRepo.save(user);

            return ResponseEntity.ok(Map.of("success", true));
        }

        // 2. 일반로그인이면 비밀번호 체크
        String password = payload.get("password");

        if (password == null || password.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "현재 비밀번호를 입력해주세요."));

        } else if (!passwordEncoder.matches(password, user.getPassword())) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "기존 비밀번호가 맞지 않습니다."));

        } else {

            // 최종 탈퇴처리
            user.setStatus(UserStatus.WITHDRAWN);
            userRepo.save(user);
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/withdrawal/done")
    public String withdrawalDoneFragment() {

        return "fragments/mypage/withdrawalDone";
    }

    @GetMapping("/grade")
    public String gradeFragment(Principal principal, Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));

        MemberGradeStatsDto stats = userService.evaluate(user);

        model.addAttribute("user", user);
        model.addAttribute("memberGrade", stats.getGrade());
        model.addAttribute("count1Year", stats.getCount1Year());
        model.addAttribute("count5Years", stats.getCount5Years());
        model.addAttribute("maxPrice", stats.getMaxPrice());
        model.addAttribute("totalPrice5Years", stats.getTotalPrice5Years());

        return "fragments/mypage/grade";
    }
}
