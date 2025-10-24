package renewal.awesome_travel;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller(value = "/")
public class MainController {

    @GetMapping
    public String main(Model model, Principal principal) {

        model.addAttribute("engineTest", "타임리프 테스트");
        if (principal != null) {
            model.addAttribute("name", principal.getName());
        }
        return "layout";
    }

    @GetMapping("/fragments/home")
    public String homeFragment() {
        // fragment만 반환
        return "fragments/home :: homeFragment";
    }

    @GetMapping("/fragments/wish")
    public String wishFragment() {
        return "fragments/wish :: wishFragment";
    }

    @GetMapping("/fragments/mypage")
    public String mypageFragment(HttpSession session) {
        Object user = session.getAttribute("user"); // 로그인 정보 확인
        if (user == null) {
            // 로그인 안 됐으면 login fragment 반환
            return "fragments/login :: loginFragment";
        }
        // 로그인 되어 있으면 mypage fragment 반환
        return "fragments/mypage :: mypageFragment";
    }

    @GetMapping("/fragments/login")
    public String loginFragment() {
        return "fragments/login :: loginFragment";
    }

    @GetMapping("login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // 로그인 되어 있으면 홈으로 리다이렉트
            return "redirect:/";
        }
        return "login";
    }
}
