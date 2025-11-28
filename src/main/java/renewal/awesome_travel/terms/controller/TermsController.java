package renewal.awesome_travel.terms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/terms")
public class TermsController {

    @GetMapping("/travel")
    public String getTravelTerms(
            @RequestParam(required = false, defaultValue = "domestic") String type,
            Model model) {
        model.addAttribute("type", type);
        return "fragments/terms/travelTerms";
    }

    @GetMapping("/service")
    public String getServiceTerms(Model model) {
        return "fragments/terms/serviceTerms";
    }

    @GetMapping("/privacy")
    public String getPrivacyPolicy(Model model) {
        return "fragments/terms/privacyPolicy";
    }
}

