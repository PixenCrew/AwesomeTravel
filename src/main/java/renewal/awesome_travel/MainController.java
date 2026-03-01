package renewal.awesome_travel;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.product.service.ProductService;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.awesome_travel.user.service.UserService;
import renewal.common.entity.Banner;
import renewal.common.entity.MenuCode;
import renewal.common.entity.Product;
import renewal.common.entity.Promotion;
import renewal.common.entity.Tour;
import renewal.common.entity.User;
import renewal.common.repository.MenuCodeRepository;
import renewal.common.repository.ProductRepository;
import renewal.common.repository.PromotionRepository;
import renewal.common.service.ProductServiceCommon;
import renewal.awesome_travel.banner.dto.repository.BannerRepository;
import renewal.common.entity.Popup;
import renewal.awesome_travel.popup.repository.PopupRepository;

@RequiredArgsConstructor
@Controller(value = "/")
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private final ProductServiceCommon productServiceCommon;
    private final UserService userService;
    private final UserRepository userRepo;
    private final ProductService productService;
    private final ProductRepository productRepo;
    private final PromotionRepository promotionRepo;
    private final MenuCodeRepository menuCodeRepo;
    private final BannerRepository bannerRepo;
    private final PopupRepository popupRepo;

    @GetMapping
    public String main(Principal principal, HttpServletRequest request, Model model) {

        // 배너 목록 가져오기 (현재 날짜 기준 활성화된 홈페이지 배너)
        LocalDate today = LocalDate.now();
        List<Banner> banners = bannerRepo.findByLocationTypeAndActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDisplayOrderAsc(
                Banner.BannerLocationType.HOME, today, today);
        model.addAttribute("banners", banners);

        // 팝업 목록 가져오기 (현재 날짜 기준 활성화된 팝업)
        List<Popup> popups = popupRepo.findActivePopupsByDate(today);
        model.addAttribute("popups", popups);
        model.addAttribute("today", today);

        // ============로그인 한 경우=================
        if (principal != null) {
            User user = userRepo.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                Hibernate.initialize(user.getRecentProducts());
                Hibernate.initialize(user.getLikedProducts());

                List<Product> actualRecentProducts = productService.convertToProducts(user.getRecentProducts());
                List<Product> actualLikedProducts = productService.convertToProducts(user.getLikedProducts());

                // 로그인 상태 → User의 element collections 사용
                model.addAttribute("currentUser", user);

            // 추천상품 100일치 항공권 필터링 적용
            List<Product> filteredRecentProducts = new ArrayList<>();
            for (Product product : actualRecentProducts) {
                if (product == null || product.getCutoffDays() == null) {
                    continue;
                }
                Hibernate.initialize(product.getTour());
                Tour tour = product.getTour();
                if (tour == null) {
                    continue;
                }
                
                Product calcedProduct = null;
                int plusDays = product.getCutoffDays().intValue();
                int maxPlusDays = plusDays + 100;
                
                while (calcedProduct == null && plusDays < maxPlusDays) {
                    LocalDate targetDate = LocalDate.now().plusDays(plusDays);
                    
                    // Tour의 startDate와 endDate 범위 체크
                    if (tour.getStartDate() != null && targetDate.isBefore(tour.getStartDate())) {
                        plusDays++;
                        continue;
                    }
                    if (tour.getEndDate() != null && targetDate.isAfter(tour.getEndDate())) {
                        break; // endDate를 넘어가면 더 이상 체크할 필요 없음
                    }
                    
                    try {
                        Product productCopy = (Product) product.clone();
                        Tour tourCopy = productServiceCommon.copyTourForOption(product.getTour());
                        if (tourCopy != null) productCopy.setTour(tourCopy);
                        calcedProduct = productServiceCommon.calcSingleProduct(productCopy, targetDate);
                    } catch (CloneNotSupportedException e) {
                        calcedProduct = productServiceCommon.calcSingleProduct(product, targetDate);
                    }
                    plusDays++;
                }
                
                if (calcedProduct != null) {
                    filteredRecentProducts.add(calcedProduct);
                }
            }
            
                model.addAttribute("recentProducts", filteredRecentProducts);
                model.addAttribute("likedProducts", actualLikedProducts);
                // ElementCollection의 개수 사용 (UserLikedProduct 엔티티가 아님)
                model.addAttribute("likedProductsCount", user.getLikedProducts() != null ? user.getLikedProducts().size() : 0);
                model.addAttribute("userCouponsCount", userService.getAvailableCoupons(user).size());
            }
        }
        if (principal == null) {
            // 비로그인 상태 → 최근 등록된 상품 5개 (인덱스 사용으로 빠름)
            Pageable pageable = PageRequest.of(0, 5);
            List<Product> recentProducts = productRepo.findRecentProducts(pageable);

            // 추천상품 100일치 항공권 필터링 적용
            List<Product> filteredRecentProducts = new ArrayList<>();
            for (Product product : recentProducts) {
                if (product == null || product.getCutoffDays() == null) {
                    continue;
                }
                Hibernate.initialize(product.getTour());
                Tour tour = product.getTour();
                if (tour == null) {
                    continue;
                }
                
                Product calcedProduct = null;
                int plusDays = product.getCutoffDays().intValue();
                int maxPlusDays = plusDays + 100;
                
                while (calcedProduct == null && plusDays < maxPlusDays) {
                    LocalDate targetDate = LocalDate.now().plusDays(plusDays);
                    
                    // Tour의 startDate와 endDate 범위 체크
                    if (tour.getStartDate() != null && targetDate.isBefore(tour.getStartDate())) {
                        plusDays++;
                        continue;
                    }
                    if (tour.getEndDate() != null && targetDate.isAfter(tour.getEndDate())) {
                        break; // endDate를 넘어가면 더 이상 체크할 필요 없음
                    }
                    
                    try {
                        Product productCopy = (Product) product.clone();
                        Tour tourCopy = productServiceCommon.copyTourForOption(product.getTour());
                        if (tourCopy != null) productCopy.setTour(tourCopy);
                        calcedProduct = productServiceCommon.calcSingleProduct(productCopy, targetDate);
                    } catch (CloneNotSupportedException e) {
                        calcedProduct = productServiceCommon.calcSingleProduct(product, targetDate);
                    }
                    plusDays++;
                }
                
                if (calcedProduct != null) {
                    filteredRecentProducts.add(calcedProduct);
                }
            }
            
            model.addAttribute("recentProducts", filteredRecentProducts);
            model.addAttribute("likedProducts", Collections.emptyList());
        }

        // 타임딜 5개
        List<Product> resulProducts = new ArrayList<>();
        List<Product> timeDealProducts = productRepo.findActiveTimeDealProducts();
        
        if (log.isDebugEnabled()) {
            log.debug("타임딜 상품 조회: {} 건", timeDealProducts.size());
        }
        for (Product product : timeDealProducts) {
            if (product == null || product.getCutoffDays() == null) {
                if (log.isDebugEnabled()) log.debug("타임딜 상품 스킵: product가 null이거나 cutoffDays가 null");
                continue;
            }
            if (product.getIsActive() == null || !product.getIsActive()) {
                if (log.isDebugEnabled()) log.debug("타임딜 상품 스킵: isActive가 false - productId={}, title={}", product.getId(), product.getTitle());
                continue;
            }
            Hibernate.initialize(product.getTour());
            Tour tour = product.getTour();
            if (tour == null) {
                if (log.isDebugEnabled()) log.debug("타임딜 상품 스킵: Tour가 null - productId={}", product.getId());
                continue;
            }

            Product calcedProduct = null;
            int plusDays = product.getCutoffDays().intValue();
            int maxPlusDays = product.getCutoffDays().intValue() + 60;

            while (calcedProduct == null && plusDays < maxPlusDays) {
                LocalDate targetDate = LocalDate.now().plusDays(plusDays);
                
                // Tour의 startDate와 endDate 범위 체크
                if (tour.getStartDate() != null && targetDate.isBefore(tour.getStartDate())) {
                    plusDays++;
                    continue;
                }
                if (tour.getEndDate() != null && targetDate.isAfter(tour.getEndDate())) {
                    break; // endDate를 넘어가면 더 이상 체크할 필요 없음
                }
                
                try {
                    Product productCopy = (Product) product.clone();
                    Tour tourCopy = productServiceCommon.copyTourForOption(product.getTour());
                    if (tourCopy != null) productCopy.setTour(tourCopy);
                    calcedProduct = productServiceCommon.calcSingleProduct(productCopy, targetDate);
                } catch (CloneNotSupportedException e) {
                    // Clone 실패 시 원본 사용
                    calcedProduct = productServiceCommon.calcSingleProduct(product, targetDate);
                }
                plusDays++;
            }

            if (calcedProduct != null) {
                if (log.isDebugEnabled()) log.debug("타임딜 상품 추가: productId={}, title={}", product.getId(), product.getTitle());
                resulProducts.add(calcedProduct);
            } else {
                if (log.isDebugEnabled()) log.debug("타임딜 상품 스킵: calcSingleProduct null - productId={}, title={}", product.getId(), product.getTitle());
            }

            if (resulProducts.size() >= 5) {
                break;
            }

        }
        if (log.isDebugEnabled()) {
            log.debug("최종 타임딜 상품 수: {}", resulProducts.size());
        }
        model.addAttribute("timeDealProducts", resulProducts);

        // 기획전 5개
        List<Promotion> promotions = promotionRepo.findActivePromotions();
        List<Promotion> resultPromotions = new ArrayList<>();
        if (log.isDebugEnabled()) {
            log.debug("기획전 조회: {} 건", promotions.size());
        }
        for (Promotion promotion : promotions) {
            resultPromotions.add(promotion);
            if (resultPromotions.size() >= 5) {
                break;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("최종 기획전 수: {}", resultPromotions.size());
        }
        model.addAttribute("promotions", resultPromotions);

        return "layout";
    }

    @GetMapping("wish")
    public String wish(Principal principal, Model model) {
        // ============로그인 한 경우=================
        if (principal != null) {
            User user = userRepo.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
            Hibernate.initialize(user.getRecentProducts());
            Hibernate.initialize(user.getLikedProducts());

            List<Product> actualRecentProducts = productService.convertToProducts(user.getRecentProducts());
            List<Product> actualLikedProducts = productService.convertToProducts(user.getLikedProducts());

            // 로그인 상태 → User의 element collections 사용
            model.addAttribute("currentUser", user);
            model.addAttribute("recentProducts", actualRecentProducts);
            model.addAttribute("likedProducts", actualLikedProducts);
            }
        }
        if (principal == null || model.getAttribute("currentUser") == null) {
            model.addAttribute("currentUser", null);
            model.addAttribute("recentProducts", Collections.emptyList());
            model.addAttribute("likedProducts", Collections.emptyList());
        }

        return "fragments/wish";
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
            return "fragments/error/dataUnavailable";

        List<MenuCode> menuCodes = menuCodeRepo.findAllByCodeStartingWith(menuCode); // 앞 3자리로 시작하는 MenuCode들

        // 각 menuCode에 해당하는 첫 번째 상품을 찾아서 Map으로 저장 (100일치 항공권 필터링 적용)
        java.util.Map<String, Product> menuCodeProductMap = new java.util.HashMap<>();
        for (MenuCode code : menuCodes) {
            List<Product> products = productService.findProductsByMenuCode(code);
            if (!products.isEmpty()) {
                // 100일치 항공권 필터링을 통해 항공권이 있는 첫 번째 상품 찾기
                Product foundProduct = null;
                for (Product product : products) {
                    if (product == null || product.getCutoffDays() == null) {
                        continue;
                    }
                    Hibernate.initialize(product.getTour());
                    Tour tour = product.getTour();
                    if (tour == null) {
                        continue;
                    }
                    
                    Product calcedProduct = null;
                    int plusDays = product.getCutoffDays().intValue();
                    int maxPlusDays = plusDays + 100;
                    
                    while (calcedProduct == null && plusDays < maxPlusDays) {
                        LocalDate targetDate = LocalDate.now().plusDays(plusDays);
                        
                        // Tour의 startDate와 endDate 범위 체크
                        if (tour.getStartDate() != null && targetDate.isBefore(tour.getStartDate())) {
                            plusDays++;
                            continue;
                        }
                        if (tour.getEndDate() != null && targetDate.isAfter(tour.getEndDate())) {
                            break; // endDate를 넘어가면 더 이상 체크할 필요 없음
                        }
                        
                        try {
                            Product productCopy = (Product) product.clone();
                            Tour tourCopy = productServiceCommon.copyTourForOption(product.getTour());
                            if (tourCopy != null) productCopy.setTour(tourCopy);
                            calcedProduct = productServiceCommon.calcSingleProduct(productCopy, targetDate);
                        } catch (CloneNotSupportedException e) {
                            calcedProduct = productServiceCommon.calcSingleProduct(product, targetDate);
                        }
                        plusDays++;
                    }
                    
                    if (calcedProduct != null) {
                        foundProduct = calcedProduct;
                        break;
                    }
                }
                if (foundProduct != null) {
                    menuCodeProductMap.put(code.getCode(), foundProduct);
                }
            }
        }

        // 서브메뉴 배너 조회
        LocalDate today = LocalDate.now();
        List<Banner> subMenuBanners = bannerRepo.findByLocationTypeAndLocationIdentifierAndActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDisplayOrderAsc(
                Banner.BannerLocationType.SUB_MENU, menuCode, today, today);

        model.addAttribute("menuCodes", menuCodes);
        model.addAttribute("menuCodeProductMap", menuCodeProductMap);
        model.addAttribute("banners", subMenuBanners);

        return "fragments/subMain/" + menuCode;
    }

    // 특정 메뉴코드 상품 목록
    @GetMapping("menu/{menuCode}")
    public String getMenuByCode(@PathVariable String menuCode, Model model) {
        try {
            if (menuCode == null || menuCode.length() != 6) {
                return renderDataError(model, "메뉴 정보를 확인할 수 없습니다.", "선택한 메뉴 코드가 올바르지 않습니다.");
            }

            MenuCode targetCode = menuCodeRepo.findByCode(menuCode);
            if (targetCode == null) {
                return renderDataError(model, "메뉴 정보를 확인할 수 없습니다.", "등록되지 않은 메뉴 코드입니다.");
            }
            
            List<Product> codeProducts = productService.findProductsByMenuCode(targetCode);
            List<Product> resulProducts = new ArrayList<>();

            List<MenuCode> relatedMenus = new ArrayList<>();
            if (targetCode.getCode() != null && targetCode.getCode().length() >= 3) {
                String prefix = targetCode.getCode().substring(0, 3);
                relatedMenus = menuCodeRepo.findAllByCodeStartingWith(prefix);
            }

            for (Product product : codeProducts) {
                if (product == null || product.getCutoffDays() == null) {
                    continue;
                }
                Hibernate.initialize(product.getTour());
                Tour tour = product.getTour();
                if (tour == null) {
                    continue;
                }

                Product calcedProduct = null;
                int plusDays = product.getCutoffDays().intValue();
                int maxPlusDays = plusDays + 100;

                while (calcedProduct == null && plusDays < maxPlusDays) {
                    LocalDate targetDate = LocalDate.now().plusDays(plusDays);
                    
                    // Tour의 startDate와 endDate 범위 체크
                    if (tour.getStartDate() != null && targetDate.isBefore(tour.getStartDate())) {
                        plusDays++;
                        continue;
                    }
                    if (tour.getEndDate() != null && targetDate.isAfter(tour.getEndDate())) {
                        break; // endDate를 넘어가면 더 이상 체크할 필요 없음
                    }
                    
                    try {
                        Product productCopy = (Product) product.clone();
                        Tour tourCopy = productServiceCommon.copyTourForOption(product.getTour());
                        if (tourCopy != null) productCopy.setTour(tourCopy);
                        calcedProduct = productServiceCommon.calcSingleProduct(productCopy, targetDate);
                    } catch (CloneNotSupportedException e) {
                        calcedProduct = productServiceCommon.calcSingleProduct(product, targetDate);
                    }
                    plusDays++;
                }

                if (calcedProduct != null) {
                    resulProducts.add(calcedProduct);
                } else {
                    // 조건 안 맞아도 상품 노출 (대표 가격 없이 원본 상품 추가)
                    resulProducts.add(product);
                }
            }

            // 메뉴에 속한 상품이 하나도 없을 때만 에러 페이지
            if (codeProducts.isEmpty()) {
                return renderDataError(model, "상품 정보를 불러올 수 없습니다.", "현재 준비 중인 상품이거나 데이터가 아직 등록되지 않았습니다.");
            }

            model.addAttribute("products", resulProducts);
            model.addAttribute("menuCode", targetCode); // MenuCode 추가 (지역명 표시용)
            model.addAttribute("menuCodeOptions", relatedMenus);

            return "fragments/product/productResult.html";
        } catch (Exception e) {
            return renderDataError(model, "상품 정보를 불러오지 못했습니다.", "잠시 후 다시 시도해 주세요.");
        }
    }

    private String renderDataError(Model model, String title, String message) {
        model.addAttribute("errorTitle", title);
        model.addAttribute("errorMessage", message);
        return "fragments/error/dataUnavailable";
    }

    // 원래 요청 URL 저장 (로그인 전)
    @PostMapping("/api/save-original-url")
    @ResponseBody
    public void saveOriginalUrl(@RequestBody java.util.Map<String, String> request, HttpSession session) {
        String url = request.get("url");
        if (url != null && !url.isEmpty()) {
            session.setAttribute("originalRequestUrl", url);
        }
    }

    // 항공편 예약 정보 저장 (로그인 전)
    @PostMapping("/api/save-air-reservation")
    @ResponseBody
    public void saveAirReservation(@RequestBody java.util.Map<String, Object> request, HttpSession session) {
        // 항공편 예약 정보를 세션에 저장
        session.setAttribute("pendingAirReservation", request);
    }

    // 항공편 예약 정보 확인 (로그인 후)
    @GetMapping("/api/check-air-reservation")
    @ResponseBody
    public java.util.Map<String, Object> checkAirReservation(HttpSession session) {
        // 세션에 저장된 항공편 예약 정보 확인
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> airReservationInfo = (java.util.Map<String, Object>) session.getAttribute("airReservationData");
        if (airReservationInfo != null) {
            session.removeAttribute("airReservationData");
            session.removeAttribute("redirectToAirReservation");
            return airReservationInfo;
        }
        return java.util.Collections.emptyMap();
    }

    // 결제 페이지로 리다이렉트할 URL 저장 (로그인 전)
    @PostMapping("/api/save-payment-redirect")
    @ResponseBody
    public void savePaymentRedirect(@RequestBody java.util.Map<String, String> request, HttpSession session) {
        String url = request.get("url");
        if (url != null && !url.isEmpty()) {
            session.setAttribute("redirectToPayment", true);
            session.setAttribute("paymentRedirectUrl", url);
        }
    }

    // 결제 페이지로 리다이렉트할 URL 확인 (로그인 후)
    @GetMapping("/api/check-payment-redirect")
    @ResponseBody
    public java.util.Map<String, String> checkPaymentRedirect(HttpSession session) {
        String redirectUrl = (String) session.getAttribute("paymentRedirectUrl");
        if (redirectUrl != null) {
            session.removeAttribute("redirectToPayment");
            session.removeAttribute("paymentRedirectUrl");
            return java.util.Map.of("url", redirectUrl);
        }
        return java.util.Collections.emptyMap();
    }

    @GetMapping("timedeal")
    public String getTimeDealList(Model model) {

        // 타임딜 5개
        List<Product> resulProducts = new ArrayList<>();
        List<Product> timeDealProducts = productRepo.findActiveTimeDealProducts();

        for (Product product : timeDealProducts) {
            if (product == null || product.getCutoffDays() == null) {
                continue;
            }
            Hibernate.initialize(product.getTour());
            Tour tour = product.getTour();
            if (tour == null) {
                continue;
            }

            Product calcedProduct = null;
            int plusDays = product.getCutoffDays().intValue();
            int maxPlusDays = product.getCutoffDays().intValue() + 60;

            while (calcedProduct == null && plusDays < maxPlusDays) {
                LocalDate targetDate = LocalDate.now().plusDays(plusDays);
                
                // Tour의 startDate와 endDate 범위 체크
                if (tour.getStartDate() != null && targetDate.isBefore(tour.getStartDate())) {
                    plusDays++;
                    continue;
                }
                if (tour.getEndDate() != null && targetDate.isAfter(tour.getEndDate())) {
                    break; // endDate를 넘어가면 더 이상 체크할 필요 없음
                }
                
                try {
                    Product productCopy = (Product) product.clone();
                    Tour tourCopy = productServiceCommon.copyTourForOption(product.getTour());
                    if (tourCopy != null) productCopy.setTour(tourCopy);
                    calcedProduct = productServiceCommon.calcSingleProduct(productCopy, targetDate);
                } catch (CloneNotSupportedException e) {
                    calcedProduct = productServiceCommon.calcSingleProduct(product, targetDate);
                }
                plusDays++;
            }

            if (calcedProduct != null) {
                resulProducts.add(calcedProduct);
            }
        }
        model.addAttribute("title", "타임딜");
        model.addAttribute("image", "/images/timedeal/banner.png");
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
            if (product == null || product.getCutoffDays() == null) {
                continue;
            }
            Hibernate.initialize(product.getTour());
            Tour tour = product.getTour();
            if (tour == null) {
                continue;
            }

            Product calcedProduct = null;
            int plusDays = product.getCutoffDays().intValue();
            int maxPlusDays = plusDays + 100;

            while (calcedProduct == null && plusDays < maxPlusDays) {
                LocalDate targetDate = LocalDate.now().plusDays(plusDays);
                
                // Tour의 startDate와 endDate 범위 체크
                if (tour.getStartDate() != null && targetDate.isBefore(tour.getStartDate())) {
                    plusDays++;
                    continue;
                }
                if (tour.getEndDate() != null && targetDate.isAfter(tour.getEndDate())) {
                    break; // endDate를 넘어가면 더 이상 체크할 필요 없음
                }
                
                try {
                    Product productCopy = (Product) product.clone();
                    Tour tourCopy = productServiceCommon.copyTourForOption(product.getTour());
                    if (tourCopy != null) productCopy.setTour(tourCopy);
                    calcedProduct = productServiceCommon.calcSingleProduct(productCopy, targetDate);
                } catch (CloneNotSupportedException e) {
                    calcedProduct = productServiceCommon.calcSingleProduct(product, targetDate);
                }
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
