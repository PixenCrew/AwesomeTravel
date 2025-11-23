package renewal.awesome_travel;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.product.service.ProductService;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.awesome_travel.user.service.UserService;
import renewal.common.entity.MenuCode;
import renewal.common.entity.Product;
import renewal.common.entity.Promotion;
import renewal.common.entity.User;
import renewal.common.entity.User.RecentViewedItem;
import renewal.common.repository.MenuCodeRepository;
import renewal.common.repository.ProductRepository;
import renewal.common.repository.PromotionRepository;
import renewal.common.service.ProductServiceCommon;

@RequiredArgsConstructor
@Controller(value = "/")
public class MainController {

    private final ProductServiceCommon productServiceCommon;
    private final UserService userService;
    private final UserRepository userRepo;
    private final ProductService productService;
    private final ProductRepository productRepo;
    private final PromotionRepository promotionRepo;
    private final MenuCodeRepository menuCodeRepo;

    @GetMapping
    public String main(Principal principal, HttpServletRequest request, Model model) {

        // ============로그인 한 경우=================
        if (principal != null) {
            User user = userRepo.findByEmail(principal.getName()).get();
            Hibernate.initialize(user.getRecentProducts());
            Hibernate.initialize(user.getLikedProducts());

            List<Product> actualRecentProducts = productService.convertToProducts(user.getRecentProducts());
            List<Product> actualLikedProducts = productService.convertToProducts(user.getLikedProducts());

            // 로그인 상태 → User의 element collections 사용
            model.addAttribute("currentUser", user);

            model.addAttribute("recentProducts", actualRecentProducts);
            model.addAttribute("likedProducts", actualLikedProducts);
            model.addAttribute("likedProductsCount", userService.getLikedProducts(user).size());
            model.addAttribute("userCouponsCount", userService.getAvailableCoupons(user).size());

        } else {
            // 비로그인 상태 → 쿠키에서 최근 본 상품만
            List<RecentViewedItem> cookieRecent = productService.loadRecentViewProducts(request);

            List<Product> actualRecentProducts = productService.convertToProducts(cookieRecent);

            model.addAttribute("recentProducts", actualRecentProducts);
            model.addAttribute("likedProducts", Collections.emptyList());
        }

        // 타임딜 5개
        List<Product> resulProducts = new ArrayList<>();
        List<Product> timeDealProducts = productRepo.findActiveTimeDealProducts();

        for (Product product : timeDealProducts) {

            Product calcedProduct = null;
            int plusDays = product.getCutoffDays().intValue();
            int maxPlusDays = product.getCutoffDays().intValue() + 30;

            while (calcedProduct == null && plusDays < maxPlusDays) {
                calcedProduct = productServiceCommon.calcSingleProduct(product, LocalDate.now().plusDays(plusDays));
                plusDays++;
            }

            if (calcedProduct != null) {
                resulProducts.add(calcedProduct);
            }

            if (resulProducts.size() >= 5) {
                break;
            }

        }
        model.addAttribute("timeDealProducts", resulProducts);

        // 기획전 5개
        List<Promotion> promotions = promotionRepo.findActivePromotions();
        List<Promotion> resultPromotions = new ArrayList<>();

        for (Promotion promotion : promotions) {
            resultPromotions.add(promotion);
            if (resultPromotions.size() >= 5) {
                break;
            }
        }
        model.addAttribute("promotions", resultPromotions);

        return "layout";
    }

    @GetMapping("login")
    public String loginPage(Authentication authentication) throws InterruptedException {
        Thread.sleep(200);
        if (authentication != null && authentication.isAuthenticated()) {
            // 로그인 되어 있으면 홈으로 리다이렉트
            return "redirect:/";
        }
        return "fragments/login";
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
        List<Product> resulProducts = new ArrayList<>();

        for (Product product : codeProducts) {

            Product calcedProduct = null;
            int plusDays = product.getCutoffDays().intValue();
            int maxPlusDays = product.getCutoffDays().intValue() + 30;

            while (calcedProduct == null && plusDays < maxPlusDays) {
                calcedProduct = productServiceCommon.calcSingleProduct(product, LocalDate.now().plusDays(plusDays));
                plusDays++;
            }

            if (calcedProduct != null) {
                resulProducts.add(calcedProduct);
            }

        }

        model.addAttribute("products", resulProducts);

        return "fragments/product/productResult";
    }

    @GetMapping("timedeal")
    public String getTimeDealList(Model model) {

        // 타임딜 5개
        List<Product> resulProducts = new ArrayList<>();
        List<Product> timeDealProducts = productRepo.findActiveTimeDealProducts();

        for (Product product : timeDealProducts) {

            Product calcedProduct = null;
            int plusDays = product.getCutoffDays().intValue();
            int maxPlusDays = product.getCutoffDays().intValue() + 30;

            while (calcedProduct == null && plusDays < maxPlusDays) {
                calcedProduct = productServiceCommon.calcSingleProduct(product, LocalDate.now().plusDays(plusDays));
                plusDays++;
            }

            if (calcedProduct != null) {
                resulProducts.add(calcedProduct);
            }
        }
        model.addAttribute("title", "타임딜");
        model.addAttribute("products", resulProducts);

        return "fragments/product/productResult";
    }

    @GetMapping("promotion")
    public String getPromotionList(Model model) {

        List<Promotion> promotions = promotionRepo.findActivePromotions();
        model.addAttribute("promotions", promotions);

        return "fragments/product/promotionList";
    }

    @GetMapping("promotion/{id}")
    public String getPromotionDetail(@PathVariable Long id, Model model) {

        Promotion promotion = promotionRepo.findById(id).orElseThrow();
        MenuCode targetCode = promotion.getMenuCode();

        List<Product> codeProducts = productService.findProductsByMenuCode(targetCode);
        List<Product> resulProducts = new ArrayList<>();

        for (Product product : codeProducts) {

            Product calcedProduct = null;
            int plusDays = product.getCutoffDays().intValue();
            int maxPlusDays = product.getCutoffDays().intValue() + 30;

            while (calcedProduct == null && plusDays < maxPlusDays) {
                calcedProduct = productServiceCommon.calcSingleProduct(product, LocalDate.now().plusDays(plusDays));
                plusDays++;
            }

            if (calcedProduct != null) {
                resulProducts.add(calcedProduct);
            }

        }

        model.addAttribute("promotion", promotion);
        model.addAttribute("products", resulProducts);

        return "fragments/product/promotionDetail";
    }

}
