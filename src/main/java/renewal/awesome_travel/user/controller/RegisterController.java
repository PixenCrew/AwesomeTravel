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

        // verifyCode 코드 맞는지 확인
        String number = payload.get("number");
        String verifyCode = payload.get("verifyCode");

        if (!number.equals(tempUser.getNumber()) || !verifyCode.equals(tempUser.getVerifyCode())) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "휴대폰 인증 정보가 올바르지 않습니다."));
        }

        // 회원가입 진행
        tempUser.setEmail(payload.get("email"));
        tempUser.setName(payload.get("name"));
        tempUser.setNumber(payload.get("number"));
        tempUser.setPassword(payload.get("password"));
        userService.register(tempUser);

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/email")
    public String verifyEmail(@RequestParam String token, Model model) {

        boolean success = false;
        String message = "";

        try {
            userService.verifyEmail(token); // 성공하면 success = true
            success = true;
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
