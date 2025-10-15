package renewal.awesome_travel.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.awesome_travel.user.dto.request.PasswordChangeRequestDto;
import renewal.awesome_travel.user.dto.request.UserRegisterRequestDto;
import renewal.awesome_travel.user.dto.request.UserUpdateRequestDto;
import renewal.awesome_travel.user.dto.response.EmailCheckResponseDto;
import renewal.awesome_travel.user.dto.response.UserResponseDto;
import renewal.awesome_travel.user.service.UserService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    //회원가입
    // 🔹 회원가입 + 인증 메일 발송
    @PostMapping("/register")
    public ResponseEntity<Long> register(@RequestBody @Valid UserRegisterRequestDto dto) {
        Long userId = userService.register(dto); // 내부에서 이메일 전송까지 수행
        return ResponseEntity.ok(userId);
    }

    // 이메일 인증 링크 클릭 시 검증
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }

    //이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<EmailCheckResponseDto> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.checkEmail(email));
    }


    //유저상세정보 마이페이지
    @GetMapping("/mypage")
    public ResponseEntity<UserResponseDto> mypage(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(userService.getMyInfo(userId));
    }


    //패스워드 변경
    @PatchMapping("/user/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PasswordChangeRequestDto dto
    ) {
        userService.changePassword(userDetails.getUser().getId(), dto);
        return ResponseEntity.ok().build();
    }

    //회원정보 수정
    @PatchMapping("/mypage")
    public ResponseEntity<Void> updateUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserUpdateRequestDto dto
    ) {
        userService.updateUserInfo(userDetails.getUser().getId(), dto);
        return ResponseEntity.ok().build();
    }

    //회원탈퇴
    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.withdraw(userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    // OAuth2 테스트
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal OAuth2User principal, Model model) {
        model.addAttribute("name", principal.getAttribute("name"));
        model.addAttribute("email", principal.getAttribute("email"));
        return "profile";
    }
}

