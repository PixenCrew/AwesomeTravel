package renewal.awesome_travel.user.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.user.dto.request.UserRegisterRequestDto;
import renewal.awesome_travel.user.entity.EmailVerificationToken;
import renewal.awesome_travel.user.repository.EmailVerificationTokenRepository;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.awesome_travel.user.service.UserService;
import renewal.common.entity.User;
import renewal.common.service.EmailService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recovery")
public class AccountRecoveryController {

    private final UserRepository userRepo;
    private final UserService userService;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/id")
    public String recoveryIdForm(HttpSession session) {

        // RegisterController 인증번호 로직 재활용
        // 임시 새 사용자 DTO 생성
        UserRegisterRequestDto tempUser = new UserRegisterRequestDto();
        session.setAttribute("tempUser", tempUser);
        return "fragments/recovery/recoverId";
    }

    @PostMapping("/id")
    public ResponseEntity<?> recoveryIdPost(@RequestBody Map<String, String> payload, HttpSession session) {

        UserRegisterRequestDto tempUser = (UserRegisterRequestDto) session.getAttribute("tempUser");

        // verifyCode 코드 맞는지 확인
        String number = payload.get("number");
        String verifyCode = payload.get("verifyCode");

        if (!number.equals(tempUser.getNumber()) || !verifyCode.equals(tempUser.getVerifyCode())) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "휴대폰 인증 정보가 올바르지 않습니다."));
        }

        User user = userRepo.findByPhone(number);
        if (user == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "등록된 휴대폰 번호가 아닙니다."));
        }

        tempUser.setEmail(user.getEmail());

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/id/result")
    public String recoveryIdResult(HttpSession session, Model model) {

        UserRegisterRequestDto tempUser = (UserRegisterRequestDto) session.getAttribute("tempUser");
        if (tempUser == null)
            return "error/error";

        String emailResult = tempUser.getEmail();

        // register controller 인증번호 로직 재활용
        session.setAttribute("tempUser", tempUser);

        model.addAttribute("emailResult", emailResult);
        session.removeAttribute("tempUser"); // 세션 임시정보 삭제

        return "fragments/recovery/recoverIdResult";
    }

    @GetMapping("/password")
    public String recoveryPasswordForm() {
        return "fragments/recovery/recoverPassword";
    }

    @PostMapping("/password")
    @Transactional
    public ResponseEntity<?> recoveryPasswordPost(@RequestBody Map<String, String> payload) {

        try {
            User user = userRepo.findByEmail(payload.get("email"))
                    .orElseThrow(() -> new IllegalArgumentException("등록된 이메일이 아닙니다."));

            // 기존 토큰 삭제 (중복 방지) - 트랜잭션 내에서 즉시 반영
            tokenRepository.deleteAllByUser(user);
            tokenRepository.flush(); // 삭제를 즉시 DB에 반영

            // 새 토큰 생성 및 이메일 발송
            EmailVerificationToken token = EmailVerificationToken.create(user);
            tokenRepository.save(token);
            emailService.sendVerificationMail(user.getEmail(), token.getToken(), "/recovery/password/verified?token="); // 비동기

            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "이메일 발송 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/password/verified")
    public String recoveryPasswordVerified(@RequestParam String token, HttpSession session) {

        EmailVerificationToken evt = tokenRepository.findById(token)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 인증 링크입니다."));

        User user = evt.getUser();
        UserRegisterRequestDto tempUser = new UserRegisterRequestDto();
        tempUser.setEmail(user.getEmail());

        session.setAttribute("tempUser", tempUser);

        return "fragments/recovery/recoverPasswordNew";
    }

    @PostMapping("/password/verified")
    @Transactional
    public ResponseEntity<?> recoveryPasswordResult(@RequestBody Map<String, String> payload, HttpSession session) {

        UserRegisterRequestDto tempUser = (UserRegisterRequestDto) session.getAttribute("tempUser");
        if (tempUser == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "만료된 세션입니다."));
        }

        User user = userRepo.findByEmail(tempUser.getEmail()).get();
        String newPassword = payload.get("newPassword");
        String newPassword2 = payload.get("newPassword2");

        if (!newPassword.equals(newPassword2)) {

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

            // 토큰 삭제
            tokenRepository.deleteAllByUser(user);
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

}
