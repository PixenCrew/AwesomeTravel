package renewal.awesome_travel;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.awesome_travel.product.service.ProductService;
import renewal.awesome_travel.user.service.UserService;
import renewal.common.entity.MenuCode;
import renewal.common.entity.Product;
import renewal.common.entity.User;
import renewal.common.repository.MenuCodeRepository;

@RequiredArgsConstructor
@Controller(value = "/")
public class MainController {

    private final UserService userService;
    private final ProductService productService;
    private final MenuCodeRepository menuCodeRepo;

    @GetMapping
    public String main(Model model, Principal principal) {

        model.addAttribute("engineTest", "타임리프 테스트");
        if (principal != null) {
            model.addAttribute("name", principal.getName());
        }
        return "layout";
    }

    @GetMapping("home")
    public String homeFragment() {
        // fragment만 반환
        return "fragments/home :: homeFragment";
    }

    @GetMapping("wish")
    public String wishFragment() {
        return "fragments/wish :: wishFragment";
    }

    @GetMapping("mypage")
    public String mypageFragment(@AuthenticationPrincipal CustomUserDetails principal, Model model) {

        User user = principal.getUser(); // detached 상태

        if (user == null) {
            // 로그인 안 됐으면 login fragment 반환
            return "fragments/login :: loginFragment";
        }

        // 마이페이지에서만 컬렉션 조회
        model.addAttribute("likedProductsCount", userService.getLikedProducts(user).size());
        model.addAttribute("userCouponsCount", userService.getAvailableCoupons(user).size());

        // 로그인 되어 있으면 mypage fragment 반환
        return "fragments/mypage :: mypageFragment";
    }

    @GetMapping("login")
    public String loginFragment(Authentication authentication) throws InterruptedException {
        Thread.sleep(200);
        if (authentication != null && authentication.isAuthenticated()) {
            // 로그인 되어 있으면 홈으로 리다이렉트
            return "redirect:/";
        }
        return "fragments/login :: loginFragment";
    }

    // 특정 메뉴코드 서브메인
    @GetMapping("subMain/{menuCode}")
    public String getSubmain(@PathVariable String menuCode, Model model) {
        if (String.valueOf(menuCode).length() != 3)
            return "error/error";

        List<MenuCode> menuCodes = menuCodeRepo.findAllByCodeStartingWith(menuCode); // 앞 3자리로 시작하는 MenuCode들

        model.addAttribute("menuCodes", menuCodes);

        return "fragments/subMain/" + menuCode;
    }

    // 특정 메뉴코드 상품 목록
    @GetMapping("menu/{menuCode}")
    public String getMenuByCode(@PathVariable String menuCode, Model model) {
        if (String.valueOf(menuCode).length() != 6)
            return "error/error";

        MenuCode targetCode = menuCodeRepo.findByCode(menuCode);
        List<Product> codeProducts = productService.findProductsByMenuCode(targetCode);
        List<Product> resulProducts = productService.calcProduct(codeProducts, LocalDate.now());

        model.addAttribute("products", resulProducts);

        return "fragments/product/productResult.html";
    }

}
