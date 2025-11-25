package renewal.awesome_travel.product.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.inquiry.dto.request.InquiryRequestDto;
import renewal.awesome_travel.inquiry.repository.InquiryRepository;
import renewal.awesome_travel.payment.dto.PaymentRequest;
import renewal.awesome_travel.payment.repository.PaymentRepository;
import renewal.awesome_travel.product.dto.ProductCalanderDto;
import renewal.awesome_travel.product.dto.ProductDetailDto;
import renewal.awesome_travel.product.dto.ProductSearchRequestDto;
import renewal.awesome_travel.product.dto.ReservationFormDto;
import renewal.awesome_travel.product.service.ProductService;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.awesome_travel.user.service.UserService;
import renewal.common.dto.ReservationRequestDto;
import renewal.common.entity.AirportCode;
import renewal.common.entity.Inquiry;
import renewal.common.entity.Inquiry.InquiryCategory;
import renewal.common.entity.Inquiry.InquiryStatus;
import renewal.common.entity.Location;
import renewal.common.entity.Location.LocationType;
import renewal.common.entity.Passenger.AgeGroup;
import renewal.common.entity.PassengerProduct;
import renewal.common.entity.Payment;
import renewal.common.entity.Product;
import renewal.common.entity.Product.ProductStatus;
import renewal.common.entity.PurchaseBase.ConfirmedSeatClass;
import renewal.common.entity.PurchaseBase.PurchaseStatus;
import renewal.common.entity.PurchaseProduct;
import renewal.common.entity.Schedule;
import renewal.common.entity.User;
import renewal.common.entity.User.MemberGrade;
import renewal.common.entity.User.RecentViewedItem;
import renewal.common.repository.ProductRepository;
import renewal.common.repository.PurchaseProductRepository;
import renewal.common.service.EmailService;
import renewal.common.service.PassengerServiceCommon;
import renewal.common.service.ProductServiceCommon;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final ProductServiceCommon productServiceCommon;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final UserService userService;
    private final PassengerServiceCommon passengerServiceCommon;
    private final PurchaseProductRepository purchaseProductRepo;
    private final PaymentRepository paymentRepo;
    private final InquiryRepository inquiryRepo;
    private final EmailService emailService;

    // @GetMapping
    // public String getProductSearch(Model model) {

    // model.addAttribute("searchRequest", new ProductSearchRequestDto());

    // List<Product> products = productRepo.findAll(); // 전체 상품들
    // LocalDate today = LocalDate.now();

    // List<Product> availProducts = productService.calcProduct(products, today);

    // model.addAttribute("products", availProducts);

    // return "product/productSearch";
    // }

    @PostMapping("/search")
    public String postProductSearch(@RequestBody ProductSearchRequestDto searchRequest, Model model) {

        List<Product> result = null;
        if (searchRequest.getKeyword() != null) {
            result = productService.searchProducts(searchRequest);
        }

        List<Product> calcedResult = new ArrayList<>();
        if (result != null) {
            for (Product product : result) {

                Product calcedProduct = null;
                int plusDays = product.getCutoffDays().intValue();
                int maxPlusDays = product.getCutoffDays().intValue() + 100;

                while (calcedProduct == null && plusDays < maxPlusDays) {
                    calcedProduct = productServiceCommon.calcSingleProduct(product, LocalDate.now().plusDays(plusDays));
                }

                if (calcedProduct != null) {
                    calcedResult.add(calcedProduct);
                }
            }
        }

        model.addAttribute("products", calcedResult);
        model.addAttribute("title", searchRequest.getKeyword() + " 검색결과");
        return "fragments/product/productResult";
    }

    @GetMapping("/{id}")
    public String getProduct(@PathVariable Long id, Model model) throws CloneNotSupportedException {

        Product target = productRepo.findById(id).get();

        List<ProductCalanderDto> result = new ArrayList<>();

        // 특정 상품에 대해 6개월간 calc해서 return
        LocalDate minDepartDate = LocalDate.now().plusDays(target.getCutoffDays()); // 기본 cutoff 계산
        for (int i = 0; i < 180; i++) {
            LocalDate targetDate = minDepartDate.plusDays(i);

            Product productCopy = (Product) target.clone();

            Product calcProduct = productServiceCommon.calcSingleProduct(productCopy, targetDate);
            if (calcProduct != null) {
                ProductCalanderDto productDto = new ProductCalanderDto(calcProduct);
                result.add(productDto);
                calcProduct.setDepartDateTime(null); // 한 Product에 대해 출발일 필드 초기화 / 안하면 출국시간 첫 값 고정
            }

            calcProduct = null;
            productCopy = null;
        }

        model.addAttribute("products", result);

        return "fragments/product/productCalander";
    }

    @GetMapping("/detail/{id}")
    public String getProductDetail(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departDate,
            Model model,
            Principal principal,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response) {

        Product target = productRepo.findById(id).orElseThrow();
        Product calcProduct = productServiceCommon.calcSingleProduct(target, departDate);

        // DTO 생성
        ProductDetailDto productDto = new ProductDetailDto(calcProduct);

        // 세션에 저장
        session.setAttribute("calcProduct", calcProduct);
        session.setAttribute("departDate", departDate);

        model.addAttribute("product", productDto);
        model.addAttribute("departDate", departDate); // 주문용 출발일 기록 (hidden)

        if (principal != null) {
            User user = userRepo.findByEmail(principal.getName()).orElseThrow();

            // 최근 본 상품 저장
            productService.saveRecentViewToDB(user, id, LocalDateTime.now());

            // 로그인한 사용자가 찜한 상품인지 여부 확인
            boolean wished = productService.wished(user, id);
            model.addAttribute("wished", wished);
        } else {
            // 비로그인: 쿠키에 저장
            productService.saveRecentViewToCookie(request, response, id, LocalDateTime.now());
        }

        return "fragments/product/productDetail";
    }

    @PostMapping("/detail/{id}/wish")
    @ResponseBody
    public Map<String, Boolean> toggleWish(
            @PathVariable Long id,
            Principal principal) {

        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        List<RecentViewedItem> list = user.getLikedProducts();

        boolean removed = list.removeIf(item -> id.equals(item.getProductId()));
        if (!removed) {
            list.add(new RecentViewedItem(id, LocalDateTime.now()));
        }
        userRepo.save(user);

        return Map.of("wished", !removed);
    }

    @PostMapping("/reservation")
    public String showPurchasePage(@RequestBody ReservationFormDto request, Model model, HttpSession session) {

        // 세션에서 상품 불러오기
        Product calcedProduct = (Product) session.getAttribute("calcProduct");

        Long adult = request.getAdult();
        Long youth = request.getYouth();
        Long infant = request.getInfant();

        if (calcedProduct.getAvailableSeats() < adult + youth) {
            model.addAttribute("isWaiting", true);
        } else {
            model.addAttribute("isWaiting", false);
        }

        // 빈 PassengerProduct들 생성
        List<PassengerProduct> passengers = passengerServiceCommon.createBlankPassengersProduct(
                adult.intValue(),
                youth.intValue(),
                infant.intValue());

        // bak (HOTEL 숙박 횟수 계산)
        Long il;
        Long bak;
        if (calcedProduct.getTour() != null && calcedProduct.getTour().getSchedules() != null) {
            bak = calcedProduct.getTour().getSchedules().stream()
                    .filter(Objects::nonNull)
                    .flatMap(s -> s.getLocations().stream())
                    .filter(loc -> loc != null && loc.getLocationType() == LocationType.HOTEL)
                    .count();
        } else {
            bak = 0L;
        }

        // il 일수
        il = (long) calcedProduct.getTour().getSchedules().size();

        calcedProduct.setIl(il);
        calcedProduct.setBak(bak);

        Long finalPrice = calcedProduct.getFinalPriceAdult() * adult
                + calcedProduct.getFinalPriceYouth() * youth
                + calcedProduct.getFinalPriceInfant() * infant;

        model.addAttribute("passengers", passengers);
        model.addAttribute("product", calcedProduct);
        model.addAttribute("finalPrice", finalPrice);

        return "fragments/product/productReservation";
    }

    @PostMapping("/purchase")
    public String purchaseModal(@RequestBody ReservationRequestDto request, Model model, Principal principal,
            HttpSession session) {

        Product calcedProduct = (Product) session.getAttribute("calcProduct");

        // 로그인 유저 이름(ID) 가져오기
        String userEmail = principal.getName();
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

        // 총 결제가격 계산
        long adult = request.getPassengers().stream()
                .filter(p -> p.getAgeGroup() == AgeGroup.ADULT)
                .count();

        long youth = request.getPassengers().stream()
                .filter(p -> p.getAgeGroup() == AgeGroup.YOUTH)
                .count();

        long infant = request.getPassengers().stream()
                .filter(p -> p.getAgeGroup() == AgeGroup.INFANT)
                .count();

        Long finalPrice = calcedProduct.getFinalPriceAdult() * adult
                + calcedProduct.getFinalPriceYouth() * youth
                + calcedProduct.getFinalPriceInfant() * infant;

        // Purchase 객체 생성 -> 저장
        PurchaseProduct purchaseProduct = new PurchaseProduct();

        // PurchaseProduct 부분
        purchaseProduct.setProduct(calcedProduct);

        purchaseProduct.setFinalPriceAdult(calcedProduct.getFinalPriceAdult());
        purchaseProduct.setFinalPriceYouth(calcedProduct.getFinalPriceYouth());
        purchaseProduct.setFinalPriceInfant(calcedProduct.getFinalPriceInfant());

        purchaseProduct.setAirline(calcedProduct.getAirline());

        purchaseProduct.setDepartDateTime(calcedProduct.getDepartDateTime());
        purchaseProduct.setReturnDateTime(calcedProduct.getReturnDateTime());
        purchaseProduct.setBak(calcedProduct.getBak());
        purchaseProduct.setIl(calcedProduct.getIl());
        purchaseProduct.setAdultCount(adult);
        purchaseProduct.setYouthCount(youth);
        purchaseProduct.setInfantCount(infant);

        purchaseProduct.setWaiterEmail(request.getWaiterEmail());
        purchaseProduct.setWaiterNumber(request.getWaiterNumber());

        List<ConfirmedSeatClass> finalSeatClasses = new ArrayList<>();
        for (Schedule schedule : calcedProduct.getTour().getSchedules()) {
            for (Location location : schedule.getLocations()) {
                if (location.getLocationType() == LocationType.AIR) {
                    finalSeatClasses.add(new ConfirmedSeatClass(location.getSeatClass(), adult, youth, infant));

                    // 첫 항공권 항공사 저장
                    if (purchaseProduct.getAirline() == null) {
                        purchaseProduct.setAirline(location.getSeatClass().getAir().getAirline());
                    }
                }
            }
        }
        purchaseProduct.setFinalSeatClasses(finalSeatClasses);

        if (calcedProduct.getProductStatus() == ProductStatus.WAITING) {
            purchaseProduct.setWaiting(true);
        }

        // 예약 요청인수보다 잔여좌석이 적으면 예약대기 취급
        if (adult + youth > calcedProduct.getAvailableSeats()) {
            purchaseProduct.setWaiting(true); // 해당 주문은 예약대기
        }

        // PurchaseBase 부분
        purchaseProduct.setTitle(calcedProduct.getTitle());
        purchaseProduct.setPurchaseStatus(PurchaseStatus.RESERVED);
        purchaseProduct.setPrice(finalPrice);
        purchaseProduct.setUser(user);
        purchaseProduct.setName(request.getBookerName());
        purchaseProduct.setNumber(request.getBookerPhone());
        purchaseProduct.setEmail(request.getBookerEmail());

        purchaseProduct.setPurchaseDate(LocalDateTime.now());
        purchaseProduct.setPaymentDueDate(calcedProduct.getDepartDateTime().minusDays(5)); // 출발 5일전까지
        purchaseProduct.setPassengerInfoDeadline(calcedProduct.getDepartDateTime().minusDays(5));

        // PassengerDto 리스트로부터 PassengerProduct 엔티티 리스트 생성
        List<PassengerProduct> passengers = passengerServiceCommon.createPassengersFromDto(request.getPassengers());

        purchaseProduct.setPassengers(passengers);

        purchaseProductRepo.save(purchaseProduct);

        // PurchaseDetail 반환
        model.addAttribute("purchaseProduct", purchaseProduct);

        return "fragments/purchase/purchaseProductDetail";
    }

    @GetMapping("/purchase/{id}")
    String getPurchaseDetail(@PathVariable Long id, Model model) {

        // TODO Principal principal로 해당 구매id 조회 가능한 사용자인지 확인

        PurchaseProduct purchaseProduct = purchaseProductRepo.findByIdWithAll(id).get();

        model.addAttribute("purchaseProduct", purchaseProduct);

        return "fragments/purchase/purchaseProductDetail";
    }

    @GetMapping("/purchase/{id}/payment")
    String getPurchasePayForm(@PathVariable Long id, Model model) {

        PurchaseProduct purchaseProduct = purchaseProductRepo.findByIdWithAll(id).get();

        model.addAttribute("purchaseBase", purchaseProduct);
        model.addAttribute("paymentType", "product");

        return "fragments/payment";
    }

    @PostMapping("/purchase/{id}/payment")
    ResponseEntity<?> postPurchasePayForm(
            @PathVariable Long id,
            @RequestBody PaymentRequest request,
            Principal principal,
            Model model) {

        PurchaseProduct purchaseProduct = purchaseProductRepo.findByIdWithAll(id).get();
        User buyer = userRepo.findByEmail(principal.getName()).get();

        // 결제 엔티티 생성
        Payment payment = new Payment();
        payment.setUser(buyer);
        payment.setPurchaseProduct(purchaseProduct);
        payment.setPaymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod()));
        payment.setPrice(purchaseProduct.getPrice());
        payment.setPurchaseStatus(Payment.PaymentStatus.PAID);
        payment.setPurchaseDate(LocalDateTime.now());

        paymentRepo.save(payment);

        MemberGrade oldGrade = buyer.getGrade();
        MemberGrade newGrade = userService.evaluate(buyer).getGrade();

        // 등급이 상승했는지 확인 (ordinal 비교)
        if (oldGrade.ordinal() < newGrade.ordinal()) {
            buyer.setGrade(newGrade);
            userRepo.save(buyer);
            emailService.sendGradeMail(buyer.getEmail(), newGrade);
        }

        // 구매 상태 업데이트
        purchaseProduct.setPurchaseStatus(PurchaseStatus.PAID);
        purchaseProductRepo.save(purchaseProduct);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/purchase/{id}/inquiry")
    String getInquiryForm(@PathVariable Long id, Model model) {

        PurchaseProduct purchaseProduct = purchaseProductRepo.findByIdWithAll(id).get();

        model.addAttribute("purchase", purchaseProduct);

        return "fragments/inquiry/inquiryForm";
    }

    @PostMapping("/purchase/{id}/inquiry")
    ResponseEntity<?> postInquiryForm(
            @PathVariable Long id,
            @RequestBody InquiryRequestDto request,
            Principal principal,
            Model model) {

        User user = userRepo.findByEmail(principal.getName()).get();
        Inquiry inquiry = new Inquiry();

        inquiry.setCategory(InquiryCategory.ORDER);
        inquiry.setUser(user);
        inquiry.setTitle(request.getTitle());
        inquiry.setContent(request.getContent());
        inquiry.setStatus(InquiryStatus.PENDING);

        inquiryRepo.save(inquiry);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/purchase/{id}/cancel")
    ResponseEntity<?> cancelPurchase(
            @PathVariable Long id,
            @RequestBody Map<String, Object> dummyload,
            Principal principal) {
        // TODO Principal로 해당 id의 PurchaseProduct 취소 가능한지 체크

        productServiceCommon.cancelPurchase(id);
        return ResponseEntity.ok().build();
    }

    // 상품 비교정보 요청
    @GetMapping("/compare")
    public String compare(@RequestParam List<Long> ids, Model model) {
        if (ids.size() != 2) {
            return "error/error";
        }

        List<Product> list = productRepo.findAllById(ids);
        Product a = list.get(0);
        Product b = list.get(1);

        Product calcedProductA = null;
        Product calcedProductB = null;
        int cutOffDays = Math.max(a.getCutoffDays().intValue(), b.getCutoffDays().intValue());
        int plusDays = cutOffDays;
        int maxPlusDays = cutOffDays + 30;

        while ((calcedProductA == null || calcedProductB == null) && plusDays < maxPlusDays) {
            calcedProductA = productServiceCommon.calcSingleProduct(a, LocalDate.now().plusDays(plusDays));
            calcedProductB = productServiceCommon.calcSingleProduct(b, LocalDate.now().plusDays(plusDays));
            plusDays++;
        }

        AirportCode aDepart = null;
        AirportCode aArrive = null;
        String aHotel = null;

        for (Schedule schedule : a.getTour().getSchedules()) {
            for (Location location : schedule.getLocations()) {

                LocationType type = location.getLocationType();

                if (type == LocationType.AIR) {
                    if (aDepart == null) {
                        aDepart = location.getDepartAirport();
                    }
                    aArrive = location.getArriveAirport();
                }
                if (aHotel == null && type == LocationType.HOTEL) {
                    aHotel = location.getHotel().getName();
                }
            }
        }

        AirportCode bDepart = null;
        AirportCode bArrive = null;
        String bHotel = null;

        for (Schedule schedule : b.getTour().getSchedules()) {
            for (Location location : schedule.getLocations()) {

                LocationType type = location.getLocationType();

                if (type == LocationType.AIR) {
                    if (bDepart == null) {
                        bDepart = location.getDepartAirport();
                    }
                    bArrive = location.getArriveAirport();
                }
                if (bHotel == null && type == LocationType.HOTEL) {
                    bHotel = location.getHotel().getName();
                }
            }
        }

        model.addAttribute("productA", calcedProductA);
        model.addAttribute("aDepart", aDepart.getAirportKor());
        model.addAttribute("aArrive", aArrive.getAirportKor());
        model.addAttribute("aHotel", aHotel);

        model.addAttribute("productB", calcedProductB);
        model.addAttribute("bDepart", bDepart.getAirportKor());
        model.addAttribute("bArrive", bArrive.getAirportKor());
        model.addAttribute("bHotel", bHotel);

        return "fragments/product/productCompare";
    }
}
