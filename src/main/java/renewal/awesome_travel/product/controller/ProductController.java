package renewal.awesome_travel.product.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import renewal.awesome_travel.inquiry.dto.request.InquiryRequestDto;
import renewal.awesome_travel.inquiry.repository.InquiryRepository;
import renewal.awesome_travel.payment.dto.PaymentRequest;
import renewal.awesome_travel.payment.repository.PaymentRepository;
import renewal.awesome_travel.product.dto.ProductCalanderDto;
import renewal.awesome_travel.product.dto.ProductDetailDto;
import renewal.awesome_travel.product.dto.ProductSearchRequestDto;
import renewal.awesome_travel.product.dto.ReservationFormDto;
import renewal.awesome_travel.product.service.ProductService;
import renewal.awesome_travel.review.repository.ReviewRepository;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.awesome_travel.user.service.UserService;
import renewal.awesome_travel.product.dto.ProductCompareViewDto;
import renewal.awesome_travel.product.service.ProductCompareService;
import renewal.common.dto.ReservationRequestDto;
import renewal.common.entity.Inquiry;
import renewal.common.entity.Inquiry.InquiryCategory;
import renewal.common.entity.AirportCode;
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
import renewal.common.entity.TimeDeal;
import renewal.common.entity.User;
import renewal.common.entity.User.MemberGrade;
import renewal.common.entity.User.RecentViewedItem;
import renewal.common.repository.ProductRepository;
import renewal.common.repository.PurchaseProductRepository;
import renewal.common.repository.RefundRepository;
import renewal.common.repository.SeatClassRepository;
import renewal.common.entity.Refund;
import renewal.common.service.EmailService;
import renewal.common.service.PassengerServiceCommon;
import renewal.common.service.ProductServiceCommon;
import org.hibernate.Hibernate;
import renewal.common.entity.SeatClass;

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
    private final ReviewRepository reviewRepo;
    private final ProductCompareService productCompareService;
    private final SeatClassRepository seatClassRepo;
    private final RefundRepository refundRepo;
    
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @GetMapping("/search")
    public String getProductSearch(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("menuCode", null);

        if (keyword != null && !keyword.isBlank()) {
            ProductSearchRequestDto searchRequest = new ProductSearchRequestDto();
            searchRequest.setKeyword(keyword.trim());
            searchRequest.setPage(0);

            Sort sort = Sort.by("id").ascending();
            Pageable pageable = PageRequest.of(0, 50, sort);

            Page<Product> result = productService.searchProducts(searchRequest, pageable);
            model.addAttribute("searchResult", result);
            model.addAttribute("title", keyword.trim() + " 검색결과");
        } else {
            model.addAttribute("searchResult", null);
            model.addAttribute("title", "패키지 검색");
        }

        return "fragments/product/productResult";
    }

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
                    plusDays++;
                }

                if (calcedProduct != null) {
                    // price 필드가 없으면 finalPriceAdult로 설정
                    if (calcedProduct.getPrice() == null && calcedProduct.getFinalPriceAdult() != null) {
                        calcedProduct.setPrice(calcedProduct.getFinalPriceAdult());
                    }
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

        System.err.println("========== getProduct 호출됨 ==========");
        System.err.println("Product ID: " + id);
        
        Product target = productRepo.findById(id).get();
        System.err.println("Product Title: " + target.getTitle());

        List<ProductCalanderDto> result = new ArrayList<>();

        // 특정 상품에 대해 6개월간 calc해서 return
        long cutoff = target.getCutoffDays() != null ? target.getCutoffDays() : 0L;
        LocalDate minDepartDate = LocalDate.now().plusDays(cutoff); // 기본 cutoff 계산
        int itineraryDays = target.getTour() != null && target.getTour().getSchedules() != null
                ? target.getTour().getSchedules().size()
                : 1;
        Long defaultRemainSeats = target.getTour() != null && target.getTour().getMaxCapacity() != null
                ? target.getTour().getMaxCapacity()
                : 0L;
        Long defaultAdultPrice = target.getPrice() != null ? target.getPrice() : 0L;
        Long defaultYouthPrice = target.getTour() != null && target.getTour().getPriceYouth() != null
                ? target.getTour().getPriceYouth()
                : defaultAdultPrice;
        Long defaultInfantPrice = target.getTour() != null && target.getTour().getPriceInfant() != null
                ? target.getTour().getPriceInfant()
                : 0L;

        for (int i = 0; i < 180; i++) {
            LocalDate targetDate = minDepartDate.plusDays(i);

            // 같은 날짜의 모든 항공편 찾기
            List<Product> calcProducts = findMultipleProductsForDate(target, targetDate, seatClassRepo);
            
            // 각 항공편에 대해 ProductCalanderDto 생성
            for (Product calcProduct : calcProducts) {
                ProductCalanderDto productDto = new ProductCalanderDto(calcProduct);
                if (productDto.getDepartDateTime() == null) {
                    productDto.setDepartDateTime(targetDate.atStartOfDay());
                }
                if (productDto.getReturnDateTime() == null) {
                    int itineraryDaysForFallback = Math.max(1, itineraryDays);
                    productDto.setReturnDateTime(productDto.getDepartDateTime().plusDays(itineraryDaysForFallback));
                }
                if (productDto.getPrice() == null) {
                    productDto.setPrice(productDto.getFinalPriceAdult());
                }
                if (productDto.getRemainSeats() == null) {
                    productDto.setRemainSeats(defaultRemainSeats);
                }
                if (productDto.getStatus() == null) {
                    productDto.setStatus(ProductStatus.AVAILABLE);
                }
                
                result.add(productDto);
                calcProduct.setDepartDateTime(null); // 한 Product에 대해 출발일 필드 초기화 / 안하면 출국시간 첫 값 고정
            }
            
            // 항공편이 없는 날짜는 달력에 표시하지 않음 (fallback 로직 완전 제거)
        }

        model.addAttribute("products", result);

        return "fragments/product/productCalander";
    }
    
    /**
     * 같은 날짜에 여러 항공편을 찾아서 각각에 대해 Product를 계산하여 반환
     */
    private List<Product> findMultipleProductsForDate(Product product, LocalDate departDate, SeatClassRepository seatClassRepo) {
        List<Product> results = new ArrayList<>();
        
        try {
            Product productCopy = (Product) product.clone();
            Hibernate.initialize(productCopy.getTour());
            
            if (productCopy.getTour() == null || productCopy.getTour().getSchedules() == null) {
                return results;
            }
            
            // 첫 번째 출국 항공편(day 0)의 모든 항공편을 찾기
            for (Schedule sced : productCopy.getTour().getSchedules()) {
                if (sced == null || sced.getDay() != 0) {
                    continue;
                }
                
                Hibernate.initialize(sced.getLocations());
                if (sced.getLocations() == null) {
                    continue;
                }
                
                for (Location loc : sced.getLocations()) {
                    if (loc == null || loc.getLocationType() != LocationType.AIR) {
                        continue;
                    }
                    
                    AirportCode departAirport = loc.getDepartAirport();
                    AirportCode arriveAirport = loc.getArriveAirport();
                    
                    if (departAirport != null) {
                        Hibernate.initialize(departAirport);
                    }
                    if (arriveAirport != null) {
                        Hibernate.initialize(arriveAirport);
                    }
                    
                    if (departAirport == null || arriveAirport == null) {
                        continue;
                    }
                    
                    // 같은 날짜의 모든 항공편을 찾기 위해 하루 전체 시간 범위 사용 (00:00 ~ 23:59:59)
                    LocalDateTime startDateTime = departDate.atTime(0, 0);
                    LocalDateTime endDateTime = departDate.atTime(23, 59, 59);
                    
                    // 같은 날짜의 모든 항공편 조회
                    List<SeatClass> allSeats = seatClassRepo.findLowestPriceSeatsByAirportCodes(
                        startDateTime, endDateTime,
                        departAirport.getAirportCode(),
                        arriveAirport.getAirportCode(),
                        productCopy.getSeatClassTypes());
                    
                    // 각 항공편에 대해 Product 계산
                    for (SeatClass seat : allSeats) {
                        try {
                            Product productForSeat = (Product) product.clone();
                            // 첫 번째 날 항공편을 지정하기 위해 Location에 SeatClass 설정
                            for (Schedule schedule : productForSeat.getTour().getSchedules()) {
                                if (schedule.getDay() == 0) {
                                    Hibernate.initialize(schedule.getLocations());
                                    for (Location location : schedule.getLocations()) {
                                        if (location != null && location.getLocationType() == LocationType.AIR
                                            && location.getDepartAirport() != null 
                                            && location.getArriveAirport() != null
                                            && location.getDepartAirport().getAirportCode().equals(departAirport.getAirportCode())
                                            && location.getArriveAirport().getAirportCode().equals(arriveAirport.getAirportCode())) {
                                            location.setSeatClass(seat);
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            
                            Product calcProduct = productServiceCommon.calcSingleProduct(productForSeat, departDate);
                            if (calcProduct != null) {
                                results.add(calcProduct);
                            }
                        } catch (CloneNotSupportedException e) {
                            log.error("Product clone failed for seat: {}", seat.getId(), e);
                        }
                    }
                    
                    break; // 첫 번째 AIR Location만 처리
                }
                break; // day 0만 처리
            }
        } catch (CloneNotSupportedException e) {
            log.error("Product clone failed", e);
        }
        
        return results;
    }

    @GetMapping("/detail/{id}")
    public String getProductDetail(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departDate,
            @RequestParam(required = false) String departTime,
            Model model,
            Principal principal,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response) {

        Product target = productRepo.findById(id).orElseThrow();
        Product productCopy;
        try {
            productCopy = (Product) target.clone();
        } catch (CloneNotSupportedException e) {
            productCopy = target;
        }

        // 선택한 항공편의 출발 시간이 전달된 경우, 해당 시간대의 항공편을 찾아서 처리
        // 달력에서 사용자가 선택한 항공편의 정확한 정보(잔여석, 시간, 가격 등)를 표시하기 위해
        Product calcProduct = null;
        if (departTime != null && !departTime.isEmpty()) {
            // 출발 시간이 전달된 경우, 같은 날짜의 모든 항공편을 찾아서 정확히 일치하는 항공편 선택
            List<Product> calcProducts = findMultipleProductsForDate(productCopy, departDate, seatClassRepo);
            
            // departTime을 LocalTime으로 파싱 (형식: "08:40:00" 또는 "08:40")
            java.time.LocalTime targetTime = null;
            try {
                if (departTime.length() >= 5) {
                    String timeStr = departTime.substring(0, 5); // "HH:mm" 형식
                    String[] parts = timeStr.split(":");
                    if (parts.length == 2) {
                        targetTime = java.time.LocalTime.of(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1])
                        );
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse departTime: {}", departTime, e);
            }
            
            // 정확히 일치하는 항공편 찾기
            if (targetTime != null) {
                for (Product p : calcProducts) {
                    if (p.getDepartDateTime() != null) {
                        java.time.LocalTime pTime = p.getDepartDateTime().toLocalTime();
                        // 시간과 분만 비교 (초는 무시)
                        if (pTime.getHour() == targetTime.getHour() 
                            && pTime.getMinute() == targetTime.getMinute()) {
                            calcProduct = p; // 선택한 항공편의 정확한 정보 사용
                            break;
                        }
                    }
                }
            }
        }
        
        // 출발 시간으로 찾지 못한 경우, 같은 날짜의 모든 항공편 중 첫 번째 항공편 사용
        if (calcProduct == null) {
            List<Product> calcProducts = findMultipleProductsForDate(productCopy, departDate, seatClassRepo);
            if (!calcProducts.isEmpty()) {
                calcProduct = calcProducts.get(0); // 첫 번째 항공편 사용
            } else {
                // 항공편이 없는 경우 기본 로직 사용
                calcProduct = productServiceCommon.calcSingleProduct(productCopy, departDate);
            }
        }
        if (calcProduct == null) {
            calcProduct = productCopy;
            LocalDateTime departDateTime = departDate.atStartOfDay();
            int itineraryDays = target.getTour() != null && target.getTour().getSchedules() != null
                    ? target.getTour().getSchedules().size()
                    : 1;

            calcProduct.setDepartDateTime(departDateTime);
            calcProduct.setReturnDateTime(departDateTime.plusDays(Math.max(1, itineraryDays)));

            Long basePrice = target.getPrice() != null ? target.getPrice() : 0L;
            Long youthPrice = target.getTour() != null && target.getTour().getPriceYouth() != null
                    ? target.getTour().getPriceYouth()
                    : basePrice;
            Long infantPrice = target.getTour() != null && target.getTour().getPriceInfant() != null
                    ? target.getTour().getPriceInfant()
                    : 0L;

            calcProduct.setFinalPriceAdult(basePrice);
            calcProduct.setFinalPriceYouth(youthPrice);
            calcProduct.setFinalPriceInfant(infantPrice);
            calcProduct.setAvailableSeats(target.getTour() != null && target.getTour().getMaxCapacity() != null
                    ? target.getTour().getMaxCapacity()
                    : 0L);
            calcProduct.setReservedSeats(0L);
            calcProduct.setProductStatus(ProductStatus.AVAILABLE);
            
            // 항공사 정보 찾기: 첫 번째 AIR Location의 정보를 사용하여 항공사 찾기
            if (calcProduct.getAirline() == null && calcProduct.getTour() != null && calcProduct.getTour().getSchedules() != null) {
                for (Schedule schedule : calcProduct.getTour().getSchedules()) {
                    if (schedule == null || schedule.getLocations() == null) {
                        continue;
                    }
                    for (Location location : schedule.getLocations()) {
                        if (location != null && location.getLocationType() == LocationType.AIR 
                                && location.getDepartAirport() != null && location.getArriveAirport() != null) {
                            // 첫 번째 AIR Location의 출발/도착 공항을 사용하여 항공사 찾기
                            // 실제 항공편 데이터에서 항공사 정보를 찾기 위해 SeatClassRepository 사용
                            try {
                                SeatClass firstSeat = seatClassRepo.findLowestPriceSeat(
                                        departDateTime,
                                        departDateTime.plusDays(1),
                                        location.getDepartAirport(),
                                        location.getArriveAirport(),
                                        target.getSeatClassTypes());
                                if (firstSeat != null && firstSeat.getAir() != null && firstSeat.getAir().getAirline() != null) {
                                    calcProduct.setAirline(firstSeat.getAir().getAirline());
                                    break;
                                }
                            } catch (Exception e) {
                                // 항공사 정보를 찾을 수 없으면 무시
                            }
                        }
                        if (calcProduct.getAirline() != null) {
                            break;
                        }
                    }
                    if (calcProduct.getAirline() != null) {
                        break;
                    }
                }
            }
        }

        // 리뷰 조회
        Pageable reviewPageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<renewal.common.entity.Review> reviewPage = reviewRepo.findByProductId(id, reviewPageable);
        List<renewal.common.entity.Review> reviews = reviewPage.getContent();
        
        // 리뷰의 writer 초기화 (LAZY 로딩 해제)
        for (renewal.common.entity.Review review : reviews) {
            Hibernate.initialize(review.getWriter());
        }

        // DTO 생성
        ProductDetailDto productDto = new ProductDetailDto(calcProduct);
        productDto.setReviews(reviews);

        List<ReviewRepository.RatingCount> ratingCounts = reviewRepo.countByProductIdGroupByRating(id);
        long star1 = 0L, star2 = 0L, star3 = 0L, star4 = 0L, star5 = 0L;
        for (ReviewRepository.RatingCount rc : ratingCounts) {
            if (rc == null || rc.getRating() == null || rc.getCount() == null) {
                continue;
            }
            switch (rc.getRating()) {
                case 1:
                    star1 = rc.getCount();
                    break;
                case 2:
                    star2 = rc.getCount();
                    break;
                case 3:
                    star3 = rc.getCount();
                    break;
                case 4:
                    star4 = rc.getCount();
                    break;
                case 5:
                    star5 = rc.getCount();
                    break;
                default:
                    break;
            }
        }
        productDto.setStar1(star1);
        productDto.setStar2(star2);
        productDto.setStar3(star3);
        productDto.setStar4(star4);
        productDto.setStar5(star5);

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
            model.addAttribute("currentUserId", user.getId()); // 현재 로그인한 사용자 ID 추가
        } else {
            // 비로그인: 쿠키에 저장
            productService.saveRecentViewToCookie(request, response, id, LocalDateTime.now());
            model.addAttribute("currentUserId", null); // 비로그인 시 null
        }

        return "fragments/product/productDetail";
    }

    @PostMapping("/detail/{id}/wish")
    @ResponseBody
    @Transactional
    public Map<String, Boolean> toggleWish(
            @PathVariable Long id,
            Principal principal) {

        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        
        // 영속성 컨텍스트에서 다시 로드하여 최신 상태 보장
        user = userRepo.findById(user.getId()).orElseThrow();
        
        List<RecentViewedItem> currentList = user.getLikedProducts();
        
        // 기존 리스트에서 해당 상품이 있는지 확인
        boolean wasWished = currentList.stream().anyMatch(item -> id.equals(item.getProductId()));
        
        // 새로운 리스트 생성 (ElementCollection 변경 감지를 위해)
        List<RecentViewedItem> newList = new ArrayList<>();
        
        if (wasWished) {
            // 찜 해제: 해당 상품 제외하고 새 리스트 생성
            currentList.stream()
                    .filter(item -> !id.equals(item.getProductId()))
                    .forEach(newList::add);
        } else {
            // 찜 추가: 기존 리스트 복사 후 새 항목 추가
            newList.addAll(currentList);
            newList.add(new RecentViewedItem(id, LocalDateTime.now()));
        }
        
        // 리스트 교체 (ElementCollection 변경 감지)
        user.setLikedProducts(newList);
        userRepo.saveAndFlush(user); // 즉시 DB에 반영

        return Map.of("wished", !wasWished);
    }

    @PostMapping("/reservation/save")
    @ResponseBody
    public ResponseEntity<?> saveReservationInfo(@RequestBody Map<String, Object> request, HttpSession session) {
        // 예약 정보를 세션에 저장 (로그인 전)
        session.setAttribute("pendingReservation", request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reservation/check")
    @ResponseBody
    public ResponseEntity<?> checkReservationInfo(HttpSession session) {
        // 세션에 저장된 예약 정보 확인
        @SuppressWarnings("unchecked")
        Map<String, Object> reservationInfo = (Map<String, Object>) session.getAttribute("reservationData");
        if (reservationInfo != null) {
            session.removeAttribute("reservationData");
            session.removeAttribute("redirectToReservation");
            return ResponseEntity.ok(reservationInfo);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reservation")
    public String showPurchasePage(@RequestBody ReservationFormDto request, Model model, HttpSession session, Principal principal) {

        // 세션에서 상품 불러오기
        Product calcedProduct = (Product) session.getAttribute("calcProduct");
        
        // 세션에 calcProduct가 없고 예약 정보가 있으면 상품 상세 페이지를 다시 로드
        if (calcedProduct == null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reservationData = (Map<String, Object>) session.getAttribute("reservationData");
            if (reservationData != null && reservationData.get("productId") != null && reservationData.get("departDate") != null) {
                try {
                    Long productId = Long.valueOf(reservationData.get("productId").toString());
                    LocalDate departDate = LocalDate.parse(reservationData.get("departDate").toString());
                    
                    Product target = productRepo.findById(productId).orElseThrow();
                    calcedProduct = productServiceCommon.calcSingleProduct(target, departDate);
                    session.setAttribute("calcProduct", calcedProduct);
                    session.setAttribute("departDate", departDate);
                } catch (Exception e) {
                    // 상품을 찾을 수 없으면 에러 처리
                    model.addAttribute("error", "상품 정보를 찾을 수 없습니다.");
                    return "error/error";
                }
            }
        }

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
            if (schedule == null || schedule.getLocations() == null) {
                continue;
            }
            for (Location location : schedule.getLocations()) {
                if (location != null && location.getLocationType() == LocationType.AIR && location.getSeatClass() != null) {
                    finalSeatClasses.add(new ConfirmedSeatClass(location.getSeatClass(), adult, youth, infant));

                    // 첫 항공권 항공사 저장
                    if (purchaseProduct.getAirline() == null && location.getSeatClass().getAir() != null) {
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
    String getPurchaseDetail(@PathVariable Long id, Principal principal, Model model) {

        PurchaseProduct purchaseProduct = purchaseProductRepo.findByIdWithAll(id).orElse(null);
        if (purchaseProduct == null) {
            return "error/error";
        }

        // 본인의 예약인지 확인
        if (principal != null) {
            User user = userRepo.findByEmail(principal.getName()).orElse(null);
            if (user != null && !purchaseProduct.getUser().getId().equals(user.getId())) {
                return "error/error"; // 다른 사용자의 예약 접근 시 에러
            }
        } else {
            return "error/error"; // 로그인하지 않은 사용자 접근 시 에러
        }

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
        purchaseProduct.setIsTransactionComplete(true);
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
            @RequestBody Map<String, Object> request,
            Principal principal) {

        // 본인의 예약인지 확인
        if (principal != null) {
            User user = userRepo.findByEmail(principal.getName()).orElse(null);
            PurchaseProduct purchaseProduct = purchaseProductRepo.findById(id).orElse(null);
            if (user == null || purchaseProduct == null || !purchaseProduct.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "권한이 없습니다."));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        // 환불 금액과 사유 추출
        Long amount = request.get("amount") != null ? 
                Long.parseLong(request.get("amount").toString()) : null;
        String reason = request.get("reason") != null ? 
                request.get("reason").toString() : "예약 취소 요청";

        // Payment에서 금액 조회 (결제 전이면 null일 수 있음)
        Payment payment = paymentRepo.findByPurchaseProductId(id).orElse(null);
        
        if (payment != null) {
            // 결제가 완료된 경우: 환불 요청 생성
            if (amount == null) {
                amount = payment.getPrice();
            }
            try {
                productServiceCommon.requestRefund(id, amount, reason);
                return ResponseEntity.ok(Map.of("success", true, "message", "환불 요청이 접수되었습니다. 관리자 승인 후 처리됩니다."));
            } catch (IllegalStateException e) {
                // 환불 요청 상태 확인
                Refund existingRefund = refundRepo.findByPurchaseIdAndRefundType(id, Refund.RefundType.PRODUCT)
                        .orElse(null);
                String detailedMessage = "이미 환불 요청이 진행 중입니다.";
                if (existingRefund != null) {
                    switch (existingRefund.getStatus()) {
                        case REQUESTED:
                            detailedMessage = "환불 요청이 이미 접수되어 관리자 승인 대기 중입니다. 중복 요청은 불가능합니다.";
                            break;
                        case APPROVED:
                            detailedMessage = "환불이 이미 승인되어 처리 중입니다.";
                            break;
                        case COMPLETED:
                            detailedMessage = "환불이 이미 완료되었습니다.";
                            break;
                        case REJECTED:
                            detailedMessage = "환불 요청이 거절되었습니다. 새로운 환불 요청을 진행할 수 있습니다.";
                            break;
                    }
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", detailedMessage));
            }
        } else {
            // 결제 전 예약 취소: 예약 상태만 변경
            PurchaseProduct purchaseProduct = purchaseProductRepo.findById(id).orElseThrow();
            purchaseProduct.setPurchaseStatus(PurchaseStatus.CANCELLED);
            purchaseProductRepo.save(purchaseProduct);
            return ResponseEntity.ok(Map.of("success", true, "message", "예약이 취소되었습니다."));
        }
    }

    // 상품 비교정보 요청

    @GetMapping("/compare")
    public String compareProducts(
            @RequestParam List<Long> ids,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departDate,
            Model model) {
        List<ProductCompareViewDto> compareList = productCompareService.buildCompareList(ids, departDate);
        model.addAttribute("compareList", compareList);
        model.addAttribute("compareDepartDate", departDate);
        return "fragments/product/compareDetail";
    }

}
