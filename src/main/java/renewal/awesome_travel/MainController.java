package renewal.awesome_travel;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

import org.springframework.ui.Model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller(value = "/")
public class MainController {

    @GetMapping
    public String main(Model model, Principal principal) {

        model.addAttribute("engineTest", "타임리프 테스트");
        if(principal!=null) {
            model.addAttribute("name", principal.getName());
        }
        return "main";
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
