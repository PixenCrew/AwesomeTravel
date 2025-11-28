package renewal.awesome_travel.user.controller;

import java.util.Map;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.user.dto.request.UserRegisterRequestDto;
import renewal.awesome_travel.user.service.UserService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegisterController {

    private final UserService userService;

    @GetMapping("/terms")
    public String termForm() {
        return "fragments/register/terms";
    }

    @PostMapping("/form")
    public String registerForm(@RequestBody Map<String, Boolean> payload, HttpSession session) {
        // 임시 새 사용자 DTO 생성
        UserRegisterRequestDto tempUser = new UserRegisterRequestDto();
        tempUser.setTerms(payload);

        session.setAttribute("tempUser", tempUser);

        return "fragments/register/form";
    }

    @PostMapping("/phone")
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

    @GetMapping("/resend")
    public String resendEmailForm() {
        return "fragments/register/resendEmail";
    }

    @PostMapping("/phone/check")
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

    @PostMapping
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload, HttpSession session) {

        UserRegisterRequestDto tempUser = (UserRegisterRequestDto) session.getAttribute("tempUser");

        // 세션에 tempUser가 없으면 에러
        if (tempUser == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "세션이 만료되었습니다. 다시 진행해주세요."));
        }

        // 필수 값 확인 및 검증
        String email = payload.get("email");
        String password = payload.get("password");
        String name = payload.get("name");
        String number = payload.get("number");
        String verifyCode = payload.get("verifyCode");

        // 빈 값 체크
        if (email == null || (email = email.trim()).isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "이메일을 입력해주세요."));
        }
        if (password == null || (password = password.trim()).isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "비밀번호를 입력해주세요."));
        }
        if (name == null || (name = name.trim()).isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "이름을 입력해주세요."));
        }
        if (number == null || (number = number.trim()).isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "휴대폰 번호를 입력해주세요."));
        }
        if (verifyCode == null || (verifyCode = verifyCode.trim()).isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "인증번호를 입력해주세요."));
        }

        // 이메일 형식 검증
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "올바른 이메일 형식을 입력해주세요."));
        }

        // 비밀번호 길이 검증
        if (password.length() < 8) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "비밀번호는 8자 이상 입력해주세요."));
        }

        // verifyCode 코드 맞는지 확인
        if (tempUser.getNumber() == null || tempUser.getVerifyCode() == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "휴대폰 인증을 먼저 완료해주세요."));
        }

        if (!number.equals(tempUser.getNumber()) || !verifyCode.equals(tempUser.getVerifyCode())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "휴대폰 인증 정보가 올바르지 않습니다."));
        }

        try {
            // 회원가입 진행
            tempUser.setEmail(email);
            tempUser.setName(name);
            tempUser.setNumber(number);
            tempUser.setPassword(password);
            userService.register(tempUser);

            // 세션 정리
            session.removeAttribute("tempUser");

            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            // 이메일 중복 등 비즈니스 로직 오류
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            // 기타 예외
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "회원가입 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/resend-email")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");

        if (email == null || (email = email.trim()).isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "이메일을 입력해주세요."));
        }

        try {
            userService.resendVerificationEmail(email);
            return ResponseEntity.ok(Map.of("success", true, "message", "인증 메일을 다시 발송했습니다."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "인증 메일 재발송 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/done")
    public String registerDone() {
        return "fragments/register/registerDone";
    }

    @GetMapping("/email")
    public String verifyEmail(@RequestParam String token, Model model) {

        boolean success = false;
        String message = "";

        try {
            success = userService.verifyEmail(token); // 성공하면 success = true
            message = "이메일 인증이 완료되었습니다.";
        } catch (IllegalArgumentException e) {
            message = "잘못된 인증 링크입니다.";
        } catch (IllegalStateException e) {
            message = "인증 링크가 만료되었습니다.";
        } catch (Exception e) {
            message = "인증 처리 중 오류가 발생했습니다.";
        }

        model.addAttribute("success", success);
        model.addAttribute("message", message);

        return "verifyResult";
    }
}
