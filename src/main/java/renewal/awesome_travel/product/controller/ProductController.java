package renewal.awesome_travel.product.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
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
import renewal.common.entity.Location.LocationType;
import renewal.common.entity.Passenger;
import renewal.common.entity.Passenger.AgeGroup;
import renewal.common.entity.Product;
import renewal.common.entity.PurchaseBase.PurchaseStatus;
import renewal.common.entity.PurchaseProduct;
import renewal.common.entity.User;
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departDate, Model model) {

        Product target = productRepo.findById(id).get();
        Product calcProduct = productService.calcSingleProduct(target, departDate);

        // DTO 생성
        ProductDetailDto productDto = new ProductDetailDto(calcProduct);

        model.addAttribute("product", productDto);
        model.addAttribute("departDate", departDate); // 주문용 출발일 기록 (hidden)

        return "fragments/product/productDetail";
    }

    @PostMapping("/reservation")
    public String showPurchasePage(@RequestBody ReservationFormDto request, Model model) {

        Long adult = request.getAdult();
        Long youth = request.getYouth();
        Long infant = request.getInfant();
        Long productId = request.getProductId();
        LocalDate departDate = request.getDepartDate();

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

        Product product = productRepo.findById(productId).get();
        Product calcedProduct = productService.calcSingleProduct(product, departDate);

        // bak (HOTEL 숙박 횟수 계산)
        Long il;
        Long bak;
        if (product.getTour() != null && product.getTour().getSchedules() != null) {
            bak = product.getTour().getSchedules().stream()
                    .filter(Objects::nonNull)
                    .flatMap(s -> s.getLocations().stream())
                    .filter(loc -> loc != null && loc.getLocationType() == LocationType.HOTEL)
                    .count();
        } else {
            bak = 0L;
        }

        // il 일수
        il = (long) product.getTour().getSchedules().size();

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
    public String purchaseModal(@RequestBody ReservationRequestDto request, Model model, Principal principal) {

        // 로그인 유저 이름(ID) 가져오기
        String userEmail = principal.getName();
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

        // 상품정보 조회 + 계산
        Product product = productRepo.findById(request.getProductId()).get();
        Product calcedProduct = productService.calcSingleProduct(product, request.getDepartDate());

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
        purchaseProduct.setProduct(calcedProduct);
        purchaseProduct.setPurchaseStatus(PurchaseStatus.RESERVED);
        purchaseProduct.setPrice(finalPrice);
        purchaseProduct.setUser(user);
        purchaseProduct.setName(request.getBookerName());
        purchaseProduct.setNumber(request.getBookerPhone());
        purchaseProduct.setEmail(request.getBookerEmail());
        purchaseProduct.setPurchaseDate(LocalDateTime.now());
        purchaseProduct.setPaymentDueDate(product.getDepartDateTime().minusDays(5)); // 출발 5일전까지
        purchaseProduct.setPassengerInfoDeadline(product.getDepartDateTime().minusDays(5));

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

        return "fragments/product/purchaseDetail";
    }
}
