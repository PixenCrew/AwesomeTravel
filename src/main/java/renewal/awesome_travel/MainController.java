package renewal.awesome_travel;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller(value = "/")
public class MainController {

    @GetMapping
    public String main(Model model) {
        model.addAttribute("engineTest", "타임리프 테스트");
        return "main";
    }

    @GetMapping("login")
    public String login(Model model) {
        return "login";
    }
}
