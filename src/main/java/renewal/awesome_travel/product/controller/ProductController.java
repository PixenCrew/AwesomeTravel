package renewal.awesome_travel.product.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.inquiry.dto.request.InquiryRequestDto;
import renewal.awesome_travel.inquiry.repository.InquiryRepository;
import renewal.awesome_travel.passport.dto.request.PassportDto;
import renewal.awesome_travel.passport.dto.request.PassportUpdateRequest;
import renewal.awesome_travel.payment.dto.PaymentRequest;
import renewal.awesome_travel.payment.repository.PaymentRepository;
import renewal.awesome_travel.product.dto.ProductCalanderDto;
import renewal.awesome_travel.product.dto.ProductDetailDto;
import renewal.awesome_travel.product.dto.ProductSearchRequestDto;
import renewal.awesome_travel.product.dto.ReservationFormDto;
import renewal.awesome_travel.product.dto.ReservationRequestDto;
import renewal.awesome_travel.product.dto.ReservationRequestDto.PassengerDto;
import renewal.awesome_travel.product.repository.ProductRepository;
import renewal.awesome_travel.product.service.ProductService;
import renewal.awesome_travel.purchase.repository.PurchaseProductRepository;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.common.entity.Inquiry;
import renewal.common.entity.Inquiry.InquiryCategory;
import renewal.common.entity.Inquiry.InquiryStatus;
import renewal.common.entity.Location;
import renewal.common.entity.Location.LocationType;
import renewal.common.entity.Passenger;
import renewal.common.entity.Passenger.AgeGroup;
import renewal.common.entity.Payment;
import renewal.common.entity.Product;
import renewal.common.entity.Product.ProductStatus;
import renewal.common.entity.PurchaseBase.PurchaseStatus;
import renewal.common.entity.PurchaseProduct;
import renewal.common.entity.PurchaseProduct.ConfirmedSeatClass;
import renewal.common.entity.Schedule;
import renewal.common.entity.User;
import renewal.common.repository.CountryCodeRepository;
import renewal.common.repository.PassengerRepository;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final PassengerRepository passengerRepo;
    private final PurchaseProductRepository purchaseProductRepo;
    private final PaymentRepository paymentProductRepo;
    private final CountryCodeRepository countryCodeRepo;
    private final InquiryRepository inquiryRepo;

    @GetMapping
    public String getProductSearch(Model model) {

        model.addAttribute("searchRequest", new ProductSearchRequestDto());

        List<Product> products = productRepo.findAll(); // 전체 상품들
        LocalDate today = LocalDate.now();

        List<Product> availProducts = productService.calcProduct(products, today);

        model.addAttribute("products", availProducts);

        return "product/productSearch";
    }

    @PostMapping("/search")
    public String postProductSearch(@ModelAttribute ProductSearchRequestDto searchRequest, Model model) {
        Sort sort = Sort.by("id").ascending();
        Pageable pageable = PageRequest.of(searchRequest.getPage(), 50, sort);

        Page<Product> result = null;
        if (searchRequest.getKeyword() != null) {
            result = productService.searchProducts(searchRequest, pageable);
        }

        model.addAttribute("searchResult", result);
        return "product/productResult";
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

            Product calcProduct = productService.calcSingleProduct(productCopy, targetDate);
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departDate, Model model,
            HttpSession session) {

        Product target = productRepo.findById(id).get();
        Product calcProduct = productService.calcSingleProduct(target, departDate);

        // DTO 생성
        ProductDetailDto productDto = new ProductDetailDto(calcProduct);

        // 세션에 저장
        session.setAttribute("calcProduct", calcProduct);
        session.setAttribute("departDate", departDate);

        model.addAttribute("product", productDto);
        model.addAttribute("departDate", departDate); // 주문용 출발일 기록 (hidden)

        return "fragments/product/productDetail";
    }

    @PostMapping("/reservation")
    public String showPurchasePage(@RequestBody ReservationFormDto request, Model model, HttpSession session) {

        // 세션에서 상품 불러오기
        Product calcedProduct = (Product) session.getAttribute("calcProduct");

        Long adult = request.getAdult();
        Long youth = request.getYouth();
        Long infant = request.getInfant();

        List<Passenger> passengers = new ArrayList<>();

        for (int i = 0; i < adult; i++) {
            Passenger adultPassenger = new Passenger();
            adultPassenger.setAgeGroup(AgeGroup.ADULT);
            passengers.add(adultPassenger);
        }
        for (int i = 0; i < youth; i++) {
            Passenger youthPassenger = new Passenger();
            youthPassenger.setAgeGroup(AgeGroup.YOUTH);
            passengers.add(youthPassenger);
        }
        for (int i = 0; i < infant; i++) {
            Passenger infantPassenger = new Passenger();
            infantPassenger.setAgeGroup(AgeGroup.INFANT);
            passengers.add(infantPassenger);
        }

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
        purchaseProduct.setPurchaseStatus(PurchaseStatus.RESERVED);
        purchaseProduct.setPrice(finalPrice);
        purchaseProduct.setUser(user);
        purchaseProduct.setName(request.getBookerName());
        purchaseProduct.setNumber(request.getBookerPhone());
        purchaseProduct.setEmail(request.getBookerEmail());

        purchaseProduct.setPurchaseDate(LocalDateTime.now());
        purchaseProduct.setPaymentDueDate(calcedProduct.getDepartDateTime().minusDays(5)); // 출발 5일전까지
        purchaseProduct.setPassengerInfoDeadline(calcedProduct.getDepartDateTime().minusDays(5));

        List<Passenger> passengers = new ArrayList<>();
        for (PassengerDto passengerDto : request.getPassengers()) {
            Passenger passenger = new Passenger();
            passenger.setName(passengerDto.getName());
            passenger.setBirth(passengerDto.getBirth());
            passenger.setSex(passengerDto.getGender());
            passenger.setNumber(passengerDto.getPhone());
            passenger.setEmail(passengerDto.getEmail());
            passenger.setAgeGroup(passengerDto.getAgeGroup());
            passengerRepo.save(passenger);

            passengers.add(passenger);
        }

        purchaseProduct.setPassengers(passengers);

        purchaseProductRepo.save(purchaseProduct);

        // PurchaseDetail 반환
        model.addAttribute("purchaseProduct", purchaseProduct);
        model.addAttribute("paymentInfo", "");

        return "fragments/purchase/purchaseDetail";
    }

    @GetMapping("/purchase/{id}")
    String getPurchaseDetail(@PathVariable Long id, Model model) {

        // TODO Principal principal로 해당 구매id 조회 가능한 사용자인지 확인

        PurchaseProduct purchaseProduct = purchaseProductRepo.findByIdWithAll(id).get();

        model.addAttribute("purchaseProduct", purchaseProduct);
        model.addAttribute("paymentInfo", "");

        return "fragments/purchase/purchaseDetail";
    }

    @GetMapping("/purchase/{id}/passport")
    String getPurchasePassportForm(@PathVariable Long id, Model model) {

        PurchaseProduct purchaseProduct = purchaseProductRepo.findByIdWithAll(id).get();

        model.addAttribute("passengers", purchaseProduct.getPassengers());
        model.addAttribute("purchaseProductId", id);

        return "fragments/purchase/passengerForm";
    }

    @PostMapping("/purchase/{id}/passport")
    String postPurchasePassportForm(@PathVariable Long id, @RequestBody PassportUpdateRequest request, Model model) {

        List<PassportDto> passengers = request.getPassengers();
        boolean allChecked = true;
        for (PassportDto dto : passengers) {
            // 기존 Passenger 조회
            Passenger passenger = passengerRepo.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("탑승객 ID가 유효하지 않습니다: " + dto.getId()));

            // 여권정보 업데이트
            passenger.setNationality(countryCodeRepo.findByCode(dto.getNationality()).get());
            passenger.setPassportNum(dto.getPassportNum());
            passenger.setLastName(dto.getLastName());
            passenger.setFirstName(dto.getFirstName());
            passenger.setExpire(dto.getExpire());
            passenger.setSpecialRequests(dto.getSpecialRequests());

            // 일반정보 업데이트
            passenger.setName(dto.getName());
            passenger.setBirth(dto.getBirth());
            passenger.setSex(dto.getSex());
            passenger.setNumber(dto.getNumber());
            passenger.setEmail(dto.getEmail());
            passenger.setAgeGroup(dto.getAgeGroup());

            // 해당 탑승객 정보 null 체크
            passenger.checkThisPassenger();
            if (passenger.isCompleted() == false) {
                allChecked = false;
            }

            passengerRepo.save(passenger);
        }

        PurchaseProduct purchaseProduct = purchaseProductRepo.findByIdWithAll(id).get();
        purchaseProduct.setIsPassengerInfoComplete(allChecked);

        model.addAttribute("purchaseProduct", purchaseProduct);
        model.addAttribute("paymentInfo", "");

        return "fragments/purchase/purchaseDetail";
    }

    @GetMapping("/purchase/{id}/payment")
    String getPurchasePayForm(@PathVariable Long id, Model model) {

        PurchaseProduct purchaseProduct = purchaseProductRepo.findByIdWithAll(id).get();

        model.addAttribute("purchaseProduct", purchaseProduct);

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

        paymentProductRepo.save(payment);

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

        inquiry.setCategory(InquiryCategory.PRODUCT);
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
            Principal principal,
            Model model) {

        PurchaseProduct purchaseProduct = purchaseProductRepo.findById(id).get();
        purchaseProduct.setPurchaseStatus(PurchaseStatus.CANCELLED);
        purchaseProduct.setWaiting(false);
        purchaseProductRepo.save(purchaseProduct);

        List<PurchaseProduct> candidate = purchaseProductRepo
                .findByProductAndDepartDate(
                        purchaseProduct.getProduct(),
                        purchaseProduct.getDepartDateTime().toLocalDate());

        Product product = productRepo.findById(purchaseProduct.getProduct().getId()).get();
        Product calcedProduct = productService
                .calcSingleProduct(product, purchaseProduct.getDepartDateTime().toLocalDate());

        for (PurchaseProduct candidatePP : candidate) {
            Long totalRequiredSeats = candidatePP.getAdultCount() + candidatePP.getYouthCount();
            if (candidatePP.isWaiting() && candidatePP.getPurchaseStatus() != PurchaseStatus.CANCELLED) {
                if (totalRequiredSeats <= calcedProduct.getAvailableSeats()) {

                    calcedProduct.setAvailableSeats(calcedProduct.getAvailableSeats() + totalRequiredSeats);
                    calcedProduct.UpdateProductStatus();
                    productRepo.save(calcedProduct);

                    candidatePP.setWaiting(false);
                    purchaseProductRepo.save(candidatePP);
                    // TODO candidatePP 해당 사용자 알람 보내기
                } else {
                    break; // 순번 건너뛰기 방지
                }
            }
        }

        return ResponseEntity.ok().build();
    }

}
