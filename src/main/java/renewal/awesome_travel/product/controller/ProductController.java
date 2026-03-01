package renewal.awesome_travel.product.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import renewal.common.entity.Tour;
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
import renewal.common.entity.Air;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
                int maxPlusDays = product.getCutoffDays().intValue() + 100;

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
                    
                    calcedProduct = productServiceCommon.calcSingleProduct(product, targetDate);
                    plusDays++;
                }

                if (calcedProduct != null) {
                    // price 필드가 없으면 finalPriceAdult로 설정
                    if (calcedProduct.getPrice() == null && calcedProduct.getFinalPriceAdult() != null) {
                        calcedProduct.setPrice(calcedProduct.getFinalPriceAdult());
                    }
                    calcedResult.add(calcedProduct);
                } else {
                    // 조건 안 맞아도 상품 노출: 대표 가격 없이 원본 상품 추가 (일정 선택 시 가격 확인)
                    calcedResult.add(product);
                }
            }
        }

        model.addAttribute("products", calcedResult);
        model.addAttribute("title", searchRequest.getKeyword() + " 검색결과");
        return "fragments/product/productResult";
    }

    @GetMapping("/{id}")
    public String getProduct(@PathVariable Long id, Model model) throws CloneNotSupportedException {

        Product target = productRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
        
        // isActive: 비활성 상품도 달력/상세 노출 가능하도록 유지 (항공 매칭 유무와 별개 정책).
        // 비활성 상품 접근 차단이 필요하면 아래 주석 해제.
        // if (target.getIsActive() == null || !target.getIsActive()) {
        //     log.warn("[ProductController] 비활성화된 상품 접근 시도 - Product ID: {}, Title: {}", id, target.getTitle());
        //     model.addAttribute("hasNoProducts", true);
        //     model.addAttribute("productTitle", target.getTitle());
        //     return "fragments/product/productCalander";
        // }
        
        log.info("[ProductController] 상품 조회 - Product ID: {}, Title: {}, isActive: {}, tour_id: {}",
            id, target.getTitle(), target.getIsActive(), target.getTour() != null ? target.getTour().getId() : "null");

        // 🔧 달력용과 하단 리스트용을 완전히 분리
        // 1️⃣ 달력용: 날짜별 대표 상품만 (가장 싼 SeatClass/조합)
        List<ProductCalanderDto> calendarProducts = new ArrayList<>();
        
        // 2️⃣ 하단 리스트용: 날짜별로 그룹화된 모든 선택 가능한 항공 조합
        // 날짜 키를 문자열로 변환 (YYYY-MM-DD 형식) - 템플릿에서 사용하기 위해
        Map<String, List<ProductCalanderDto>> listProductsByDate = new java.util.LinkedHashMap<>();

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

        Hibernate.initialize(target.getTour());
        Tour tour = target.getTour();
        LocalDate tourStartDate = tour != null ? tour.getStartDate() : null;
        LocalDate tourEndDate = tour != null ? tour.getEndDate() : null;

        for (int i = 0; i < 180; i++) {
            LocalDate targetDate = minDepartDate.plusDays(i);
            
            // Tour의 startDate와 endDate 범위 체크
            if (tourStartDate != null && targetDate.isBefore(tourStartDate)) {
                continue;
            }
            if (tourEndDate != null && targetDate.isAfter(tourEndDate)) {
                break; // endDate를 넘어가면 더 이상 체크할 필요 없음
            }

            // 같은 날짜의 모든 항공편 찾기 (dedup 전/후 결과 모두 반환)
            ProductCombinationResult combinationResult = findMultipleProductsForDate(target, targetDate, seatClassRepo);
            
            // 🔧 디버깅: 발리 상품(Product ID 10)에 대해서는 모든 날짜 로그 출력
            boolean shouldLog = id == 10L || (targetDate.getMonthValue() == 2 && targetDate.getYear() == 2026);
            
            if (shouldLog) {
                log.info("[ProductController] 📅 날짜별 상품 조회 - Date: {}, calendar={}, list={}", 
                    targetDate, 
                    combinationResult.getCalendarProducts().size(), 
                    combinationResult.getListProducts().size());
            }
            
            // 🔧 1️⃣ 달력용: calendarProducts 사용 (날짜별 대표 상품)
            List<ProductCalanderDto> calendarResults = new ArrayList<>();
            for (Product calcProduct : combinationResult.getCalendarProducts()) {
                // Tour/Schedules/Locations 초기화
                if (calcProduct.getTour() != null) {
                    Hibernate.initialize(calcProduct.getTour().getSchedules());
                    if (calcProduct.getTour().getSchedules() != null) {
                        for (Schedule schedule : calcProduct.getTour().getSchedules()) {
                            if (schedule != null) {
                                Hibernate.initialize(schedule.getLocations());
                                if (schedule.getLocations() != null) {
                                    for (Location location : schedule.getLocations()) {
                                        if (location != null && location.getSeatClass() != null) {
                                            Hibernate.initialize(location.getSeatClass());
                                            if (location.getSeatClass().getAir() != null) {
                                                Hibernate.initialize(location.getSeatClass().getAir());
                                                if (location.getSeatClass().getAir().getAirline() != null) {
                                                    Hibernate.initialize(location.getSeatClass().getAir().getAirline());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (calcProduct.getAirline() != null) {
                    Hibernate.initialize(calcProduct.getAirline());
                }
                ProductCalanderDto calendarDto = new ProductCalanderDto(calcProduct);
                if (calendarDto.getDepartDateTime() == null) {
                    calendarDto.setDepartDateTime(targetDate.atStartOfDay());
                }
                if (calendarDto.getReturnDateTime() == null) {
                    int itineraryDaysForFallback = Math.max(1, itineraryDays - 1);
                    calendarDto.setReturnDateTime(calendarDto.getDepartDateTime().plusDays(itineraryDaysForFallback));
                }
                if (calendarDto.getPrice() == null) {
                    calendarDto.setPrice(calendarDto.getFinalPriceAdult());
                }
                if (calendarDto.getRemainSeats() == null) {
                    calendarDto.setRemainSeats(defaultRemainSeats);
                }
                if (calendarDto.getStatus() == null) {
                    calendarDto.setStatus(ProductStatus.AVAILABLE);
                }
                calendarResults.add(calendarDto);
            }
            
            // 🔧 2️⃣ 하단 리스트용: listProducts 사용 (항공사 + 좌석등급별로 모두 유지)
            // 이미 findMultipleProductsForDate()에서 (departAirId + returnAirId + seatClassType) 기준 dedup 완료
            List<ProductCalanderDto> listResults = new ArrayList<>();
            for (Product calcProduct : combinationResult.getListProducts()) {
                // Tour/Schedules/Locations 초기화 (좌석 등급 및 항공사 정보를 가져오기 위해)
                if (calcProduct.getTour() != null) {
                    Hibernate.initialize(calcProduct.getTour().getSchedules());
                    if (calcProduct.getTour().getSchedules() != null) {
                        for (Schedule schedule : calcProduct.getTour().getSchedules()) {
                            if (schedule != null) {
                                Hibernate.initialize(schedule.getLocations());
                                if (schedule.getLocations() != null) {
                                    for (Location location : schedule.getLocations()) {
                                        if (location != null && location.getSeatClass() != null) {
                                            Hibernate.initialize(location.getSeatClass());
                                            if (location.getSeatClass().getAir() != null) {
                                                Hibernate.initialize(location.getSeatClass().getAir());
                                                if (location.getSeatClass().getAir().getAirline() != null) {
                                                    Hibernate.initialize(location.getSeatClass().getAir().getAirline());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Product의 airline 필드도 초기화
                if (calcProduct.getAirline() != null) {
                    Hibernate.initialize(calcProduct.getAirline());
                }
                ProductCalanderDto productDto = new ProductCalanderDto(calcProduct);
                if (productDto.getDepartDateTime() == null) {
                    productDto.setDepartDateTime(targetDate.atStartOfDay());
                }
                if (productDto.getReturnDateTime() == null) {
                    int itineraryDaysForFallback = Math.max(1, itineraryDays - 1);
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
                
                listResults.add(productDto);
            }
            
            // 🔧 달력용과 하단 리스트용을 분리해서 저장
            // 달력용 (대표 1개만)
            calendarProducts.addAll(calendarResults);
            
            // 하단 리스트용 (날짜별로 묶기) - 날짜 키를 문자열로 변환
            if (!listResults.isEmpty()) {
                String dateKey = targetDate.toString(); // "YYYY-MM-DD" 형식
                listProductsByDate.put(dateKey, listResults);
            }
            
            // 항공편이 없는 날짜는 달력에 표시하지 않음 (fallback 로직 완전 제거)
        }

        // 6개월치를 돌았는데도 항공권이 없는 경우
        boolean hasNoProducts = calendarProducts.isEmpty() && listProductsByDate.isEmpty();
        
        // 🔧 디버깅: hasNoProducts가 true인 경우 상세 로그 출력
        if (hasNoProducts) {
            log.warn("[ProductController] ⚠️ 판매 가능한 항공권 없음 - Product ID: {}, Title: {}, isActive: {}, calendarProducts.size(): {}, listProductsByDate.size(): {}", 
                id, target.getTitle(), target.getIsActive(), calendarProducts.size(), listProductsByDate.size());
            log.warn("[ProductController] Tour 정보 - startDate: {}, endDate: {}, cutoffDays: {}", 
                tour != null ? tour.getStartDate() : "null", 
                tour != null ? tour.getEndDate() : "null", 
                target.getCutoffDays());
            log.warn("[ProductController] minDepartDate: {}, 180일 후: {}", 
                minDepartDate, minDepartDate.plusDays(180));
        }
        
        // JSON 문자열로 직렬화하여 프론트에서 파싱 가능하도록 전달 (좌석등급 필터 등 동작 보장)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String calendarProductsJson = "[]";
        String listProductsByDateJson = "{}";
        try {
            calendarProductsJson = objectMapper.writeValueAsString(calendarProducts);
            listProductsByDateJson = objectMapper.writeValueAsString(listProductsByDate);
        } catch (JsonProcessingException e) {
            log.warn("[ProductController] 달력 데이터 JSON 직렬화 실패", e);
        }
        model.addAttribute("calendarProductsJson", calendarProductsJson);
        model.addAttribute("listProductsByDateJson", listProductsByDateJson);
        model.addAttribute("calendarProducts", calendarProducts);
        model.addAttribute("listProductsByDate", listProductsByDate);
        model.addAttribute("hasNoProducts", hasNoProducts);
        model.addAttribute("productTitle", target.getTitle());

        return "fragments/product/productCalander";
    }
    
    /**
     * 같은 날짜에 여러 항공편을 찾아서 각각에 대해 Product를 계산하여 반환
     * 모든 Schedule의 모든 AIR Location에 대해 모든 항공편 조합을 생성
     * 
     * @return ProductCombinationResult - dedup 전/후 결과를 모두 포함
     */
    private ProductCombinationResult findMultipleProductsForDate(Product product, LocalDate departDate, SeatClassRepository seatClassRepo) {
        
        // 🔧 디버깅: 발리 상품(Product ID 10)에 대해서는 상세 로그
        boolean isBaliProduct = product.getId() != null && product.getId() == 10L;
        
        try {
            Product productCopy = (Product) product.clone();
            Hibernate.initialize(productCopy.getTour());
            
            if (productCopy.getTour() == null || productCopy.getTour().getSchedules() == null) {
                if (isBaliProduct) {
                    log.warn("[ProductController] ⚠️ findMultipleProductsForDate - Tour 또는 Schedules가 null - Product ID: {}", product.getId());
                }
                return new ProductCombinationResult(new ArrayList<>(), new ArrayList<>());
            }
            
            // 모든 Schedule의 모든 AIR Location에 대해 항공편 찾기
            List<AirLocationInfo> airLocationInfos = new ArrayList<>();
            int totalSchedules = productCopy.getTour().getSchedules().size();
            int totalLocations = 0;
            int airLocationsFound = 0;
            int airLocationsWithAirports = 0;
            
            for (Schedule sced : productCopy.getTour().getSchedules()) {
                if (sced == null) {
                    continue;
                }
                
                Hibernate.initialize(sced.getLocations());
                if (sced.getLocations() == null) {
                    if (isBaliProduct) {
                        log.warn("[ProductController] ⚠️ Schedule의 Locations가 null - Product ID: {}, Schedule Day: {}", 
                            product.getId(), sced.getDay());
                    }
                    continue;
                }
                
                totalLocations += sced.getLocations().size();
                
                for (Location loc : sced.getLocations()) {
                    if (loc == null || loc.getLocationType() != LocationType.AIR) {
                        continue;
                    }
                    
                    airLocationsFound++;
                    AirportCode departAirport = loc.getDepartAirport();
                    AirportCode arriveAirport = loc.getArriveAirport();
                    
                    if (departAirport != null) {
                        Hibernate.initialize(departAirport);
                    }
                    if (arriveAirport != null) {
                        Hibernate.initialize(arriveAirport);
                    }
                    
                    if (departAirport == null || arriveAirport == null) {
                        if (isBaliProduct) {
                            log.warn("[ProductController] ⚠️ AIR Location에 공항 정보 없음 - Product ID: {}, Schedule Day: {}, Location ID: {}, departAirport: {}, arriveAirport: {}", 
                                product.getId(), sced.getDay(), loc.getId(), 
                                departAirport != null ? departAirport.getAirportCode() : "null",
                                arriveAirport != null ? arriveAirport.getAirportCode() : "null");
                        }
                        continue;
                    }
                    
                    airLocationsWithAirports++;
                    
                    // 해당 Schedule의 날짜 계산
                    LocalDate currentScheduleDate = departDate.plusDays(sced.getDay());
                    LocalDateTime startDateTime = currentScheduleDate.atTime(0, 0);
                    LocalDateTime endDateTime = currentScheduleDate.atTime(23, 59, 59);
                    
                    // 임시: 2월 데이터만 상세 로그 출력 (디버깅용)
                    boolean shouldLog = departDate.getMonthValue() == 2 && departDate.getYear() == 2026;
                    
                    if (shouldLog) {
                        log.debug("Searching seats - departDate: {}, day: {}, currentScheduleDate: {}, startDateTime: {}, endDateTime: {}, depart: {} -> arrive: {}", 
                            departDate, sced.getDay(), currentScheduleDate, startDateTime, endDateTime, 
                            departAirport.getAirportCode(), arriveAirport.getAirportCode());
                    }
                    
                    // 같은 날짜의 모든 항공편 조회 (모든 좌석 등급 조회)
                    // 좌석 등급별 필터링은 나중에 하므로, 여기서는 모든 좌석 등급을 조회
                    Set<SeatClass.SeatClassType> allSeatClassTypes = 
                        productCopy.getSeatClassTypes() != null && !productCopy.getSeatClassTypes().isEmpty()
                            ? productCopy.getSeatClassTypes()
                            : Set.of(SeatClass.SeatClassType.ECONOMY, SeatClass.SeatClassType.PREMIUMECONOMY, SeatClass.SeatClassType.BUSINESS, SeatClass.SeatClassType.FIRST);
                    List<SeatClass> allSeats = seatClassRepo.findLowestPriceSeatsByAirportCodes(
                        startDateTime, endDateTime,
                        departAirport.getAirportCode(),
                        arriveAirport.getAirportCode(),
                        allSeatClassTypes);
                    
                    // 🔧 로그 최적화: 빈 리스트일 때는 로그 출력하지 않음
                    if (shouldLog && !allSeats.isEmpty()) {
                        log.debug("Found {} seats for day {} (depart: {} -> arrive: {}), date: {}, seatClassTypes: {}", 
                            allSeats.size(), sced.getDay(), departAirport.getAirportCode(), arriveAirport.getAirportCode(), 
                            currentScheduleDate, allSeatClassTypes);
                        for (SeatClass sc : allSeats) {
                            log.debug("  SeatClass: airId={}, classType={}, price={}", 
                                sc.getAir() != null ? sc.getAir().getId() : "null",
                                sc.getClassType(), sc.getPriceAdult());
                        }
                    }
                    
                    // 🔧 핵심: 항공사별로 "최저가 노선 1개"만 선택하고, 그 노선의 이코노미/비즈니스/프리미엄 등급만 사용
                    // → 한 날짜에 "아메리칸 3개(이코노미·비즈니스·프리미엄) + 에어프랑스 3개" = 총 6개 수준으로 노출
                    Map<String, List<SeatClass>> byAirline = new java.util.HashMap<>();
                    for (SeatClass seat : allSeats) {
                        if (seat.getAir() == null || seat.getAir().getAirline() == null) continue;
                        String airlineCode = seat.getAir().getAirline().getCode();
                        if (airlineCode == null) continue;
                        byAirline.computeIfAbsent(airlineCode, k -> new ArrayList<>()).add(seat);
                    }
                    
                    List<SeatClass> uniqueSeats = new ArrayList<>();
                    for (List<SeatClass> airlineSeats : byAirline.values()) {
                        // 해당 항공사 내에서 "노선(Air)별 최저가" 구한 뒤, 그 중 가장 싼 노선 1개 선택
                        Map<Long, List<SeatClass>> byAirId = new java.util.HashMap<>();
                        for (SeatClass s : airlineSeats) {
                            byAirId.computeIfAbsent(s.getAir().getId(), k -> new ArrayList<>()).add(s);
                        }
                        Long bestAirId = null;
                        long bestMinPrice = Long.MAX_VALUE;
                        for (Map.Entry<Long, List<SeatClass>> e : byAirId.entrySet()) {
                            long minPrice = e.getValue().stream()
                                .filter(s -> s.getPriceAdult() != null)
                                .mapToLong(s -> s.getPriceAdult().longValue())
                                .min()
                                .orElse(Long.MAX_VALUE);
                            if (minPrice < bestMinPrice) {
                                bestMinPrice = minPrice;
                                bestAirId = e.getKey();
                            }
                        }
                        if (bestAirId != null) {
                            uniqueSeats.addAll(byAirId.get(bestAirId));
                        }
                    }
                    
                    // 가격순, 출발 시간순 정렬 (조합 생성 순서 고정)
                    uniqueSeats.sort((a, b) -> {
                        Long priceA = a.getPriceAdult() != null ? a.getPriceAdult() : 0L;
                        Long priceB = b.getPriceAdult() != null ? b.getPriceAdult() : 0L;
                        int priceCompare = Long.compare(priceA, priceB);
                        if (priceCompare != 0) return priceCompare;
                        if (a.getAir() != null && b.getAir() != null) {
                            return a.getAir().getDepartDateTime().compareTo(b.getAir().getDepartDateTime());
                        }
                        return 0;
                    });
                    
                    if (shouldLog && !uniqueSeats.isEmpty()) {
                        log.debug("After per-airline single flight + all seat classes: {} unique seats", uniqueSeats.size());
                        for (SeatClass sc : uniqueSeats) {
                            log.debug("  uniqueSeat: airId={}, classType={}, price={}", 
                                sc.getAir() != null ? sc.getAir().getId() : "null",
                                sc.getClassType(), sc.getPriceAdult());
                        }
                    }
                    
                    if (!uniqueSeats.isEmpty()) {
                        airLocationInfos.add(new AirLocationInfo(sced.getDay(), loc, departAirport, arriveAirport, uniqueSeats));
                        if (shouldLog) {
                            log.debug("Added AirLocationInfo for day {} (total airLocationInfos: {})", sced.getDay(), airLocationInfos.size());
                        }
                    } else {
                        if (shouldLog) {
                            log.warn("uniqueSeats is empty for day {}, loc: {} -> {}", 
                                sced.getDay(), 
                                departAirport != null ? departAirport.getAirportCode() : "null",
                                arriveAirport != null ? arriveAirport.getAirportCode() : "null");
                        }
                    }
                }
            }
            
            // 좌석 등급별로 그룹화하여 같은 등급끼리만 매칭
            // 각 좌석 등급(ECONOMY, BUSINESS 등)별로 별도의 조합 생성
            List<Product> tempProducts = new ArrayList<>();
            
            // 허용된 모든 좌석 등급에 대해 각각 조합 생성
            // 임시: 2월 데이터만 상세 로그 출력 (디버깅용)
            boolean shouldLog = departDate.getMonthValue() == 2 && departDate.getYear() == 2026;
            
            // 🔧 핵심 수정: 모든 좌석등급을 한 번에 수집하기 위해 Map을 루프 밖으로 이동
            // 키: 출국편 airId + 귀국편 airId (좌석등급별로 Product 생성하지 않음)
            // 같은 항공조합에 대해 좌석등급별 대표 SeatClass를 Product에 설정
            // 🔧 출국편과 귀국편 SeatClass를 모두 저장하기 위한 구조
            Map<String, Map<SeatClass.SeatClassType, SeatClass>> departSeatClassByFlightAndType = new java.util.HashMap<>();
            Map<String, Map<SeatClass.SeatClassType, SeatClass>> returnSeatClassByFlightAndType = new java.util.HashMap<>();
            Map<String, Product> productMapByFlightCombination = new java.util.LinkedHashMap<>();
            
            if (productCopy.getSeatClassTypes() != null && !productCopy.getSeatClassTypes().isEmpty()) {
                if (shouldLog) {
                    log.debug("Product seatClassTypes: {}", productCopy.getSeatClassTypes());
                    log.debug("Total airLocationInfos size: {}", airLocationInfos.size());
                    for (AirLocationInfo info : airLocationInfos) {
                        log.debug("AirLocationInfo - day: {}, depart: {}, arrive: {}, seatClasses count: {}", 
                            info.day, info.departAirport != null ? info.departAirport.getAirportCode() : "null",
                            info.arriveAirport != null ? info.arriveAirport.getAirportCode() : "null",
                            info.seatClasses != null ? info.seatClasses.size() : 0);
                        if (info.seatClasses != null) {
                            for (SeatClass sc : info.seatClasses) {
                                log.debug("  - SeatClass: classType={}, price={}", 
                                    sc.getClassType(), sc.getPriceAdult());
                            }
                        }
                    }
                }
                
                for (SeatClass.SeatClassType seatClassType : productCopy.getSeatClassTypes()) {
                    if (shouldLog) {
                        log.debug("Processing seatClassType: {}", seatClassType);
                    }
                    // 해당 좌석 등급만 필터링한 AirLocationInfo 리스트 생성
                    List<AirLocationInfo> filteredAirLocationInfos = new ArrayList<>();
                    for (AirLocationInfo info : airLocationInfos) {
                        List<SeatClass> filteredSeats = info.seatClasses.stream()
                            .filter(seat -> seat.getClassType() == seatClassType)
                            .collect(java.util.stream.Collectors.toList());
                        if (!filteredSeats.isEmpty()) {
                            filteredAirLocationInfos.add(new AirLocationInfo(
                                info.day, info.location, info.departAirport, info.arriveAirport, filteredSeats));
                            // 🔧 로그 최적화: shouldLog일 때만 출력
                            if (shouldLog) {
                                log.debug("  Added filteredAirLocationInfo - day: {}, seatCount: {}", info.day, filteredSeats.size());
                            }
                        }
                    }
                    
                    // 🔧 로그 최적화: 빈 리스트일 때는 로그 출력하지 않음
                    if (!filteredAirLocationInfos.isEmpty()) {
                        log.debug("SeatClassType {}: filteredAirLocationInfos.size()={}", seatClassType, filteredAirLocationInfos.size());
                    }
                    
                    // 출국편과 귀국편 모두에 해당 좌석 등급이 있어야 조합 생성
                    // 개선: size() < 2 대신 출국/귀국 존재 여부로 명시적 체크
                    Long lastDay = airLocationInfos.stream()
                        .mapToLong(info -> info.day != null ? info.day : 0L)
                        .max()
                        .orElse(0L);
                    
                    boolean hasDepart = filteredAirLocationInfos.stream()
                        .anyMatch(info -> Objects.equals(info.day, 0L));
                    boolean hasReturn = filteredAirLocationInfos.stream()
                        .anyMatch(info -> Objects.equals(info.day, lastDay));
                    
                    if (!hasDepart || !hasReturn) {
                        if (shouldLog) {
                            log.warn("SeatClassType {}: 조합 생성 불가 (출국편 또는 귀국편 중 하나에만 좌석 등급 존재). hasDepart={}, hasReturn={}, filteredAirLocationInfos.size()={}, lastDay={}", 
                                seatClassType, hasDepart, hasReturn, filteredAirLocationInfos.size(), lastDay);
                        }
                        continue; // 출국편과 귀국편 모두에 해당 좌석 등급이 없으면 스킵
                    }
                    
                    // 해당 좌석 등급의 모든 조합 생성 (카르테시안 곱)
                    List<List<SeatClassAssignment>> combinations = generateCartesianProduct(filteredAirLocationInfos);
                    
                    // 출국일(day 0)과 귀국일(마지막 day)의 항공사가 같은 조합만 필터링
                    Long firstDay = filteredAirLocationInfos.stream()
                        .mapToLong(info -> info.day != null ? info.day : 0L)
                        .min()
                        .orElse(0L);
                    // lastDay는 위에서 이미 계산됨 (airLocationInfos 기준)
                    
                    List<List<SeatClassAssignment>> filteredCombinations = new ArrayList<>();
                    for (List<SeatClassAssignment> combination : combinations) {
                        // 출국일(day 0)의 항공사 찾기
                        SeatClassAssignment departAssignment = combination.stream()
                            .filter(a -> Objects.equals(a.day, firstDay))
                            .findFirst()
                            .orElse(null);
                        
                        // 귀국일(마지막 day)의 항공사 찾기
                        SeatClassAssignment returnAssignment = combination.stream()
                            .filter(a -> Objects.equals(a.day, lastDay))
                            .findFirst()
                            .orElse(null);
                        
                        // 출국일과 귀국일의 항공사가 같으면 유지
                        if (departAssignment != null && returnAssignment != null) {
                            String departAirline = null;
                            String returnAirline = null;
                            
                            if (departAssignment.seatClass != null 
                                && departAssignment.seatClass.getAir() != null
                                && departAssignment.seatClass.getAir().getAirline() != null) {
                                departAirline = departAssignment.seatClass.getAir().getAirline().getCode();
                            }
                            
                            if (returnAssignment.seatClass != null 
                                && returnAssignment.seatClass.getAir() != null
                                && returnAssignment.seatClass.getAir().getAirline() != null) {
                                returnAirline = returnAssignment.seatClass.getAir().getAirline().getCode();
                            }
                            
                            // 항공사 코드가 같으면 유지
                            if (departAirline != null && returnAirline != null && departAirline.equals(returnAirline)) {
                                filteredCombinations.add(combination);
                            }
                        }
                    }
                    
                    // 🔧 로그 최적화: 빈 리스트일 때는 로그 출력하지 않음
                    if (!filteredCombinations.isEmpty()) {
                        log.debug("SeatClassType {}: filteredCombinations.size()={}", seatClassType, filteredCombinations.size());
                    }
                    
                    // 🔧 핵심 수정: 모든 좌석등급의 조합을 순회하여 (departAirId + returnAirId)별로 좌석등급별 최저가 SeatClass 수집
                    // seatClassByFlightAndType은 루프 밖에서 이미 생성됨
                    // 이 루프는 모든 좌석등급의 데이터를 수집만 하고, Product 생성은 나중에 한 번만 수행
                    
                    for (List<SeatClassAssignment> combination : filteredCombinations) {
                        try {
                            // 출국편과 귀국편의 airId 추출
                            Long departAirId = null;
                            Long returnAirId = null;
                            SeatClass.SeatClassType currentSeatClassType = null;
                            
                            for (SeatClassAssignment assignment : combination) {
                                if (Objects.equals(assignment.day, firstDay) && assignment.seatClass != null && assignment.seatClass.getAir() != null) {
                                    departAirId = assignment.seatClass.getAir().getId();
                                    if (currentSeatClassType == null) {
                                        currentSeatClassType = assignment.seatClass.getClassType();
                                    }
                                }
                                if (Objects.equals(assignment.day, lastDay) && assignment.seatClass != null && assignment.seatClass.getAir() != null) {
                                    returnAirId = assignment.seatClass.getAir().getId();
                                }
                            }
                            
                            // 출국편과 귀국편이 모두 있어야 처리
                            if (departAirId == null || returnAirId == null || currentSeatClassType == null) {
                                continue;
                            }
                            
                            // 항공조합 키 (좌석등급 무시)
                            String flightKey = departAirId + "_" + returnAirId;
                            
                            // 출국편과 귀국편 SeatClass 찾기
                            SeatClass departSeatClass = null;
                            SeatClass returnSeatClass = null;
                            for (SeatClassAssignment assignment : combination) {
                                if (Objects.equals(assignment.day, firstDay) && assignment.seatClass != null) {
                                    departSeatClass = assignment.seatClass;
                                }
                                if (Objects.equals(assignment.day, lastDay) && assignment.seatClass != null) {
                                    returnSeatClass = assignment.seatClass;
                                }
                            }
                            
                            // 출국/귀국 모두 최대인원만큼 잔여석이 있어야 첫 예약 시 홀딩 가능하므로, 미달 시 조합 제외
                            // 단, 해당 조합에 PackageSeatHold가 있고 풀 여유(totalHeld - allocated > 0)가 있으면 availableSeats 체크 생략
                            if (departSeatClass == null || returnSeatClass == null) {
                                continue;
                            }
                            Long maxCap = productCopy.getTour() != null && productCopy.getTour().getMaxCapacity() != null
                                ? productCopy.getTour().getMaxCapacity() : 0L;
                            if (maxCap > 0) {
                                boolean holdHasPool = productServiceCommon.hasHoldWithPoolSpace(
                                    productCopy.getId(), departDate, departSeatClass.getId(), returnSeatClass.getId());
                                if (!holdHasPool) {
                                    Long depAvail = departSeatClass.getAvailableSeats() != null ? departSeatClass.getAvailableSeats() : 0L;
                                    Long retAvail = returnSeatClass.getAvailableSeats() != null ? returnSeatClass.getAvailableSeats() : 0L;
                                    if (depAvail < maxCap || retAvail < maxCap) {
                                        continue;
                                    }
                                }
                            }
                            
                            // 🔧 출국편과 귀국편 SeatClass를 별도로 저장
                            if (departSeatClass != null) {
                                departSeatClassByFlightAndType.computeIfAbsent(flightKey, k -> new java.util.HashMap<>());
                                Map<SeatClass.SeatClassType, SeatClass> departSeatClassMap = departSeatClassByFlightAndType.get(flightKey);
                                
                                // 같은 좌석등급에 대해 최저가만 유지
                                SeatClass existing = departSeatClassMap.get(currentSeatClassType);
                                if (existing == null || 
                                    (departSeatClass.getPriceAdult() != null && 
                                     (existing.getPriceAdult() == null || departSeatClass.getPriceAdult() < existing.getPriceAdult()))) {
                                    departSeatClassMap.put(currentSeatClassType, departSeatClass);
                                }
                            }
                            
                            if (returnSeatClass != null) {
                                returnSeatClassByFlightAndType.computeIfAbsent(flightKey, k -> new java.util.HashMap<>());
                                Map<SeatClass.SeatClassType, SeatClass> returnSeatClassMap = returnSeatClassByFlightAndType.get(flightKey);
                                
                                // 같은 좌석등급에 대해 최저가만 유지
                                SeatClass existing = returnSeatClassMap.get(currentSeatClassType);
                                if (existing == null || 
                                    (returnSeatClass.getPriceAdult() != null && 
                                     (existing.getPriceAdult() == null || returnSeatClass.getPriceAdult() < existing.getPriceAdult()))) {
                                    returnSeatClassMap.put(currentSeatClassType, returnSeatClass);
                                }
                            }
                        } catch (Exception e) {
                            log.error("Failed to process combination", e);
                        }
                    }
                }
                
                // 🔧 모든 좌석등급 수집 완료 후, 각 항공조합별로 Product 생성 (좌석등급별 대표 SeatClass 설정)
                // 이 부분은 좌석등급별 루프 밖에서 한 번만 실행됨
                Long firstDay = airLocationInfos.stream()
                    .mapToLong(info -> info.day != null ? info.day : 0L)
                    .min()
                    .orElse(0L);
                Long lastDay = airLocationInfos.stream()
                    .mapToLong(info -> info.day != null ? info.day : 0L)
                    .max()
                    .orElse(0L);
                
                // 🔧 출국편과 귀국편 SeatClass를 모두 포함하는 키 집합 생성
                java.util.Set<String> allFlightKeys = new java.util.HashSet<>();
                allFlightKeys.addAll(departSeatClassByFlightAndType.keySet());
                allFlightKeys.addAll(returnSeatClassByFlightAndType.keySet());
                
                // 🔧 각 좌석등급별로 Product 생성
                Set<SeatClass.SeatClassType> allSeatClassTypes = Set.of(
                    SeatClass.SeatClassType.ECONOMY,
                    SeatClass.SeatClassType.PREMIUMECONOMY,
                    SeatClass.SeatClassType.BUSINESS,
                    SeatClass.SeatClassType.FIRST
                );
                
                for (String flightKey : allFlightKeys) {
                    Map<SeatClass.SeatClassType, SeatClass> departSeatClassMap = departSeatClassByFlightAndType.getOrDefault(flightKey, new java.util.HashMap<>());
                    Map<SeatClass.SeatClassType, SeatClass> returnSeatClassMap = returnSeatClassByFlightAndType.getOrDefault(flightKey, new java.util.HashMap<>());
                    
                    // 🔧 각 좌석등급별로 Product 생성
                    for (SeatClass.SeatClassType seatClassType : allSeatClassTypes) {
                        // 출국편과 귀국편 모두에 해당 좌석등급이 있어야 Product 생성
                        SeatClass departSeatClass = departSeatClassMap.get(seatClassType);
                        SeatClass returnSeatClass = returnSeatClassMap.get(seatClassType);
                        
                        if (departSeatClass == null || returnSeatClass == null) {
                            continue; // 해당 좌석등급이 없으면 스킵
                        }
                        
                        try {
                            // Product 생성
                            Product productForCombination = (Product) product.clone();
                            // 옵션별로 서로 다른 Tour/Location을 쓰기 위해 Tour 복사본 사용
                            Tour tourCopy = productServiceCommon.copyTourForOption(product.getTour());
                            if (tourCopy != null) {
                                productForCombination.setTour(tourCopy);
                            }

                            // 🔧 좌석등급별 대표 SeatClass 설정
                            productForCombination.setEconomySeatClass(departSeatClassMap.get(SeatClass.SeatClassType.ECONOMY));
                            productForCombination.setPremiumEconomySeatClass(departSeatClassMap.get(SeatClass.SeatClassType.PREMIUMECONOMY));
                            productForCombination.setBusinessSeatClass(departSeatClassMap.get(SeatClass.SeatClassType.BUSINESS));
                            productForCombination.setFirstSeatClass(departSeatClassMap.get(SeatClass.SeatClassType.FIRST));
                            
                            // 🔧 모든 AIR Location에 해당 좌석등급의 SeatClass를 설정 (출국편/귀국편/중간 경유편 모두)
                            // 출국편과 귀국편의 출발/도착 공항 정보를 미리 저장 (LAZY 로딩 해제)
                            String departDepartAirport = null;
                            String departArriveAirport = null;
                            String returnDepartAirport = null;
                            String returnArriveAirport = null;
                            
                            if (departSeatClass != null) {
                                Hibernate.initialize(departSeatClass);
                                if (departSeatClass.getAir() != null) {
                                    Hibernate.initialize(departSeatClass.getAir());
                                    if (departSeatClass.getAir().getDepartAirport() != null) {
                                        Hibernate.initialize(departSeatClass.getAir().getDepartAirport());
                                        departDepartAirport = departSeatClass.getAir().getDepartAirport().getAirportCode();
                                    }
                                    if (departSeatClass.getAir().getArriveAirport() != null) {
                                        Hibernate.initialize(departSeatClass.getAir().getArriveAirport());
                                        departArriveAirport = departSeatClass.getAir().getArriveAirport().getAirportCode();
                                    }
                                }
                            }
                            
                            if (returnSeatClass != null) {
                                Hibernate.initialize(returnSeatClass);
                                if (returnSeatClass.getAir() != null) {
                                    Hibernate.initialize(returnSeatClass.getAir());
                                    if (returnSeatClass.getAir().getDepartAirport() != null) {
                                        Hibernate.initialize(returnSeatClass.getAir().getDepartAirport());
                                        returnDepartAirport = returnSeatClass.getAir().getDepartAirport().getAirportCode();
                                    }
                                    if (returnSeatClass.getAir().getArriveAirport() != null) {
                                        Hibernate.initialize(returnSeatClass.getAir().getArriveAirport());
                                        returnArriveAirport = returnSeatClass.getAir().getArriveAirport().getAirportCode();
                                    }
                                }
                            }
                            
                            // 🔧 모든 Schedule의 모든 AIR Location에 SeatClass 설정 (출국편/귀국편/중간 경유편 모두)
                            // 🔥 핵심: 모든 AIR Location에 동일한 좌석등급의 SeatClass를 설정하여 혼종 계산 방지
                            boolean allAirLocationsSet = true;
                            int airLocationCount = 0;
                            int seatClassSetCount = 0;
                            
                            for (Schedule schedule : productForCombination.getTour().getSchedules()) {
                                if (schedule == null) continue;
                                Hibernate.initialize(schedule.getLocations());
                                if (schedule.getLocations() == null) continue;
                                
                                for (Location location : schedule.getLocations()) {
                                    if (location != null && location.getLocationType() == LocationType.AIR) {
                                        airLocationCount++;
                                        
                                        // Location의 출발/도착 공항 정보 가져오기
                                        String locDepartAirport = null;
                                        String locArriveAirport = null;
                                        
                                        if (location.getDepartAirport() != null) {
                                            Hibernate.initialize(location.getDepartAirport());
                                            locDepartAirport = location.getDepartAirport().getAirportCode();
                                        }
                                        if (location.getArriveAirport() != null) {
                                            Hibernate.initialize(location.getArriveAirport());
                                            locArriveAirport = location.getArriveAirport().getAirportCode();
                                        }
                                        
                                        // 🔧 1순위: 노선 매칭 (출국편/귀국편 노선과 일치하는지 확인)
                                        boolean isDepartRoute = (departDepartAirport != null && departArriveAirport != null
                                            && locDepartAirport != null && locArriveAirport != null
                                            && locDepartAirport.equals(departDepartAirport)
                                            && locArriveAirport.equals(departArriveAirport));
                                        
                                        boolean isReturnRoute = (returnDepartAirport != null && returnArriveAirport != null
                                            && locDepartAirport != null && locArriveAirport != null
                                            && locDepartAirport.equals(returnDepartAirport)
                                            && locArriveAirport.equals(returnArriveAirport));
                                        
                                        // 🔧 2순위: firstDay/lastDay 기준 (노선 매칭 실패 시)
                                        boolean isFirstDay = Objects.equals(schedule.getDay(), firstDay);
                                        boolean isLastDay = Objects.equals(schedule.getDay(), lastDay);
                                        
                                        // 🔧 SeatClass 설정 (우선순위: 노선 매칭 > firstDay/lastDay)
                                        SeatClass seatClassToSet = null;
                                        String setReason = "";
                                        
                                        if (isDepartRoute && departSeatClass != null) {
                                            seatClassToSet = departSeatClass;
                                            setReason = "출국편 노선 매칭";
                                        } else if (isReturnRoute && returnSeatClass != null) {
                                            seatClassToSet = returnSeatClass;
                                            setReason = "귀국편 노선 매칭";
                                        } else if (isFirstDay && departSeatClass != null) {
                                            seatClassToSet = departSeatClass;
                                            setReason = "첫날 (fallback)";
                                        } else if (isLastDay && returnSeatClass != null) {
                                            seatClassToSet = returnSeatClass;
                                            setReason = "마지막날 (fallback)";
                                        }
                                        
                                        if (seatClassToSet != null) {
                                            location.setSeatClass(seatClassToSet);
                                            seatClassSetCount++;
                                            log.debug("[ProductController] AIR Location SeatClass 설정 - Day: {}, Route: {}→{}, Reason: {}, ClassType: {}, AirId: {}, Price: {}", 
                                                schedule.getDay(), 
                                                locDepartAirport != null ? locDepartAirport : "null",
                                                locArriveAirport != null ? locArriveAirport : "null",
                                                setReason,
                                                seatClassToSet.getClassType() != null ? seatClassToSet.getClassType().name() : "null",
                                                seatClassToSet.getAir() != null ? seatClassToSet.getAir().getId() : "null",
                                                seatClassToSet.getPriceAdult());
                                        } else {
                                            allAirLocationsSet = false;
                                            log.warn("[ProductController] ⚠️ AIR Location에 SeatClass를 설정할 수 없음 - Day: {}, Route: {}→{}, departSeatClass: {}, returnSeatClass: {}", 
                                                schedule.getDay(), 
                                                locDepartAirport != null ? locDepartAirport : "null",
                                                locArriveAirport != null ? locArriveAirport : "null",
                                                departSeatClass != null ? "있음" : "null",
                                                returnSeatClass != null ? "있음" : "null");
                                        }
                                    }
                                }
                            }
                            
                            // 🔥 핵심 검증: 모든 AIR Location에 SeatClass가 설정되었는지 확인
                            if (!allAirLocationsSet || airLocationCount == 0 || seatClassSetCount != airLocationCount) {
                                log.warn("[ProductController] ❌ Product 생성 건너뜀 - 모든 AIR Location에 SeatClass 설정 실패 (AIR Location 수: {}, SeatClass 설정 수: {})", 
                                    airLocationCount, seatClassSetCount);
                                continue; // 조건에 맞지 않으면 Product 생성하지 않음
                            }
                            
                            // 🔥 핵심 검증: 출국편과 귀국편이 같은 좌석등급인지 확인
                            if (departSeatClass == null || returnSeatClass == null) {
                                log.warn("[ProductController] ❌ Product 생성 건너뜀 - 출국편 또는 귀국편 SeatClass가 null");
                                continue;
                            }
                            
                            SeatClass.SeatClassType departClassType = departSeatClass.getClassType();
                            SeatClass.SeatClassType returnClassType = returnSeatClass.getClassType();
                            
                            if (departClassType == null || returnClassType == null || !departClassType.equals(returnClassType)) {
                                log.warn("[ProductController] ❌ Product 생성 건너뜀 - 출국편과 귀국편의 좌석등급이 다름 (출국: {}, 귀국: {})", 
                                    departClassType != null ? departClassType.name() : "null",
                                    returnClassType != null ? returnClassType.name() : "null");
                                continue; // 좌석등급이 다르면 Product 생성하지 않음
                            }
                            
                            // calcSingleProduct 호출
                            Product calcProduct = productServiceCommon.calcSingleProduct(productForCombination, departDate);
                            if (calcProduct != null && calcProduct.getFinalPriceAdult() != null) {
                                // 🔧 키: flightKey + seatClassType (같은 항공조합이라도 좌석등급별로 별도 Product)
                                String productKey = flightKey + "_" + seatClassType.name();
                                productMapByFlightCombination.put(productKey, calcProduct);
                                log.debug("[ProductController] ✅ Product 생성 완료 - Key: {}, ClassType: {}, Price: {}", 
                                    productKey, seatClassType.name(), calcProduct.getFinalPriceAdult());
                            } else {
                                log.warn("[ProductController] ❌ Product 생성 건너뜀 - calcSingleProduct 결과가 null이거나 가격이 없음");
                            }
                        } catch (CloneNotSupportedException e) {
                            log.error("Product clone failed for flight combination and seat class: {}", seatClassType, e);
                        }
                    }
                }
                
                // Map의 값들을 tempProducts에 추가
                tempProducts.addAll(productMapByFlightCombination.values());
            } else {
                // seatClassTypes가 없는 경우 기존 로직 사용 (모든 조합 생성)
                List<List<SeatClassAssignment>> combinations = generateCartesianProduct(airLocationInfos);
                
                // 출국일(day 0)과 귀국일(마지막 day)의 항공사가 같은 조합만 필터링
                Long firstDay = airLocationInfos.stream()
                    .mapToLong(info -> info.day)
                    .min()
                    .orElse(0L);
                Long lastDay = airLocationInfos.stream()
                    .mapToLong(info -> info.day)
                    .max()
                    .orElse(0L);
                
                List<List<SeatClassAssignment>> filteredCombinations = new ArrayList<>();
                for (List<SeatClassAssignment> combination : combinations) {
                    // 출국일(day 0)의 항공사 찾기
                    SeatClassAssignment departAssignment = combination.stream()
                        .filter(a -> Objects.equals(a.day, firstDay))
                        .findFirst()
                        .orElse(null);
                    
                    // 귀국일(마지막 day)의 항공사 찾기
                    SeatClassAssignment returnAssignment = combination.stream()
                        .filter(a -> Objects.equals(a.day, lastDay))
                        .findFirst()
                        .orElse(null);
                    
                    // 출국일과 귀국일의 항공사가 같으면 유지
                    if (departAssignment != null && returnAssignment != null) {
                        String departAirline = null;
                        String returnAirline = null;
                        
                        if (departAssignment.seatClass != null 
                            && departAssignment.seatClass.getAir() != null
                            && departAssignment.seatClass.getAir().getAirline() != null) {
                            departAirline = departAssignment.seatClass.getAir().getAirline().getCode();
                        }
                        
                        if (returnAssignment.seatClass != null 
                            && returnAssignment.seatClass.getAir() != null
                            && returnAssignment.seatClass.getAir().getAirline() != null) {
                            returnAirline = returnAssignment.seatClass.getAir().getAirline().getCode();
                        }
                        
                        // 항공사 코드가 같으면 유지
                        if (departAirline != null && returnAirline != null && departAirline.equals(returnAirline)) {
                            filteredCombinations.add(combination);
                        }
                    }
                }
                
                // 🔧 핵심 수정: Product 생성 기준을 "항공편 조합"으로 변경
                // 키: 출국편 airId + 귀국편 airId + 좌석등급
                // productMapByFlightCombination은 이미 루프 밖에서 선언됨
                
                for (List<SeatClassAssignment> combination : filteredCombinations) {
                    try {
                        // 출국편과 귀국편의 airId 추출
                        Long departAirId = null;
                        Long returnAirId = null;
                        SeatClass.SeatClassType seatClassType = null;
                        
                        for (SeatClassAssignment assignment : combination) {
                            if (Objects.equals(assignment.day, firstDay) && assignment.seatClass != null && assignment.seatClass.getAir() != null) {
                                departAirId = assignment.seatClass.getAir().getId();
                                if (seatClassType == null) {
                                    seatClassType = assignment.seatClass.getClassType();
                                }
                            }
                            if (Objects.equals(assignment.day, lastDay) && assignment.seatClass != null && assignment.seatClass.getAir() != null) {
                                returnAirId = assignment.seatClass.getAir().getId();
                            }
                        }
                        
                        // 출국편과 귀국편이 모두 있어야 Product 생성
                        if (departAirId == null || returnAirId == null || seatClassType == null) {
                            continue;
                        }
                        
                        // 🔧 핵심: Product 키 생성 - 출국편 airId + 귀국편 airId + 좌석등급
                        // 같은 키면 같은 Product (가격만 최소값으로 유지)
                        String productKey = departAirId + "_" + returnAirId + "_" + seatClassType.name();
                        
                        // Product 생성 및 가격 계산
                        Product productForCombination = (Product) product.clone();
                        // 옵션별로 서로 다른 Tour/Location을 쓰기 위해 Tour 복사본 사용
                        Tour tourCopy = productServiceCommon.copyTourForOption(product.getTour());
                        if (tourCopy != null) {
                            productForCombination.setTour(tourCopy);
                        }

                        // 각 AIR Location에 SeatClass 설정
                        for (SeatClassAssignment assignment : combination) {
                            for (Schedule schedule : productForCombination.getTour().getSchedules()) {
                                if (Objects.equals(schedule.getDay(), assignment.day)) {
                                    Hibernate.initialize(schedule.getLocations());
                                    for (Location location : schedule.getLocations()) {
                                        if (location != null && location.getLocationType() == LocationType.AIR
                                            && location.getDepartAirport() != null 
                                            && location.getArriveAirport() != null
                                            && location.getDepartAirport().getAirportCode().equals(assignment.departAirport.getAirportCode())
                                            && location.getArriveAirport().getAirportCode().equals(assignment.arriveAirport.getAirportCode())) {
                                            location.setSeatClass(assignment.seatClass);
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        
                        // calcSingleProduct 호출 (이미 설정된 SeatClass는 그대로 사용)
                        Product calcProduct = productServiceCommon.calcSingleProduct(productForCombination, departDate);
                        if (calcProduct == null || calcProduct.getFinalPriceAdult() == null) {
                            continue;
                        }
                        
                        Long currentPrice = calcProduct.getFinalPriceAdult();
                        Product existingProduct = productMapByFlightCombination.get(productKey);
                        
                        // 같은 키가 없거나, 현재 가격이 더 저렴하면 추가/업데이트
                        if (existingProduct == null || 
                            (existingProduct.getFinalPriceAdult() != null && currentPrice < existingProduct.getFinalPriceAdult())) {
                            productMapByFlightCombination.put(productKey, calcProduct);
                        }
                    } catch (CloneNotSupportedException e) {
                        log.error("Product clone failed for combination", e);
                    }
                }
                
                // Map의 값들을 tempProducts에 추가
                tempProducts.addAll(productMapByFlightCombination.values());
            }
            
            // 조합 생성 순서를 고정하기 위해 정렬
            // 1. 출국 시간순 정렬
            // 2. 가격순 정렬 (같은 출국 시간에 대해 항상 같은 조합 선택)
            tempProducts.sort((a, b) -> {
                // 출국 시간 비교
                if (a.getDepartDateTime() != null && b.getDepartDateTime() != null) {
                    int timeCompare = a.getDepartDateTime().compareTo(b.getDepartDateTime());
                    if (timeCompare != 0) {
                        return timeCompare;
                    }
                }
                // 출국 시간이 같으면 가격순 정렬
                Long priceA = a.getFinalPriceAdult() != null ? a.getFinalPriceAdult() : 0L;
                Long priceB = b.getFinalPriceAdult() != null ? b.getFinalPriceAdult() : 0L;
                return Long.compare(priceA, priceB);
            });
            
            // 🔧 하단 리스트용: tempProducts를 그대로 사용 (좌석 등급별로 모두 유지)
            // 이미 위에서 (departAirId + returnAirId + seatClassType) 기준으로 dedup 완료
            // 여기서 다시 dedup하면 모든 좌석 등급이 사라짐
            
            // 🔧 달력용: calendarKey 기준으로 dedup (출국 airId + 귀국 airId, 좌석등급 무시, 최저가 1개만)
            Map<String, Product> calendarDedupProducts = new java.util.LinkedHashMap<>();
            for (Product p : tempProducts) {
                String key = calendarKey(p);
                if (key != null) {
                    Product existing = calendarDedupProducts.get(key);
                    Long existingPrice = existing != null && existing.getFinalPriceAdult() != null 
                        ? existing.getFinalPriceAdult() : Long.MAX_VALUE;
                    Long currentPrice = p.getFinalPriceAdult() != null ? p.getFinalPriceAdult() : Long.MAX_VALUE;
                    
                    if (existing == null || currentPrice < existingPrice) {
                        calendarDedupProducts.put(key, p);
                    }
                }
            }
            
            // 임시: 데이터가 있는 경우만 로그 출력 (디버깅용)
            if (!tempProducts.isEmpty()) {
                log.debug("findMultipleProductsForDate result: listProducts.size()={}, calendarProducts.size()={}", 
                    tempProducts.size(), calendarDedupProducts.size());
            }
            
            // 🔧 하단 리스트용: tempProducts에서 중복 제거 (같은 항공조합 + 같은 좌석등급 = 1개만)
            Map<String, Product> listDedupProducts = new java.util.LinkedHashMap<>();
            for (Product p : tempProducts) {
                // 키: 출국 airId + 귀국 airId + 좌석등급 + 출국시간 + 귀국시간
                String listKey = generateListProductKey(p);
                if (listKey != null) {
                    Product existing = listDedupProducts.get(listKey);
                    
                    // 🔧 같은 키면 첫 번째 상품 유지 (가격 비교 없이)
                    // 같은 항공조합 + 같은 좌석등급 + 같은 시간이면 동일한 상품이므로 첫 번째 것만 유지
                    if (existing == null) {
                        listDedupProducts.put(listKey, p);
                    }
                }
            }
            
            // 🔧 하단 리스트용: listDedupProducts 사용 (같은 항공조합 + 같은 좌석등급 = 1개만)
            // 🔧 달력용: calendarDedupProducts 사용 (출국 airId + 귀국 airId 기준, 날짜별 대표 1개만)
            return new ProductCombinationResult(
                new ArrayList<>(calendarDedupProducts.values()),  // 달력용 (출국 airId + 귀국 airId 기준 dedup)
                new ArrayList<>(listDedupProducts.values())       // 하단 리스트용 (항공조합 + 좌석등급별 dedup)
            );
        } catch (CloneNotSupportedException e) {
            log.error("Product clone failed", e);
            return new ProductCombinationResult(new ArrayList<>(), new ArrayList<>());
        }
    }
    
    /**
     * AIR Location 정보를 담는 내부 클래스
     */
    private static class AirLocationInfo {
        Long day;
        Location location;
        AirportCode departAirport;
        AirportCode arriveAirport;
        List<SeatClass> seatClasses;
        
        AirLocationInfo(Long day, Location location, AirportCode departAirport, AirportCode arriveAirport, List<SeatClass> seatClasses) {
            this.day = day;
            this.location = location;
            this.departAirport = departAirport;
            this.arriveAirport = arriveAirport;
            this.seatClasses = seatClasses;
        }
    }
    
    /**
     * SeatClass 할당 정보를 담는 내부 클래스
     */
    private static class SeatClassAssignment {
        Long day;
        AirportCode departAirport;
        AirportCode arriveAirport;
        SeatClass seatClass;
        
        SeatClassAssignment(Long day, AirportCode departAirport, AirportCode arriveAirport, SeatClass seatClass) {
            this.day = day;
            this.departAirport = departAirport;
            this.arriveAirport = arriveAirport;
            this.seatClass = seatClass;
        }
    }
    
    /**
     * findMultipleProductsForDate의 반환 결과
     * - rawProducts: dedup 전 모든 상품 (하단 리스트용)
     * - dedupProducts: dedup 후 대표 상품 (달력용)
     */
    private static class ProductCombinationResult {
        List<Product> calendarProducts;  // 날짜별 대표 1개 (달력용)
        List<Product> listProducts;      // 항공사 + 좌석등급별 (하단 리스트용)
        
        ProductCombinationResult(List<Product> calendarProducts, List<Product> listProducts) {
            this.calendarProducts = calendarProducts;
            this.listProducts = listProducts;
        }
        
        List<Product> getCalendarProducts() {
            return calendarProducts;
        }
        
        List<Product> getListProducts() {
            return listProducts;
        }
    }

    /**
     * reservationData 복구 시 동일 항공 조합을 찾기 위함.
     * 출국/귀국 시간 + 좌석등급이 일치하는 Product를 반환, 없으면 null.
     */
    private Product findMatchingCalcProduct(List<Product> calcProducts, String departTime, String returnTime, String seatClassType) {
        if (calcProducts == null || calcProducts.isEmpty() || departTime == null || departTime.isEmpty()) {
            return null;
        }
        java.time.LocalTime targetDepartTime = null;
        try {
            if (departTime.length() >= 5) {
                String timeStr = departTime.substring(0, 5);
                String[] parts = timeStr.split(":");
                if (parts.length == 2) {
                    targetDepartTime = java.time.LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse departTime: {}", departTime, e);
            return null;
        }
        if (targetDepartTime == null) {
            return null;
        }
        java.time.LocalTime targetReturnTime = null;
        if (returnTime != null && !returnTime.isEmpty()) {
            try {
                if (returnTime.length() >= 5) {
                    String timeStr = returnTime.substring(0, 5);
                    String[] parts = timeStr.split(":");
                    if (parts.length == 2) {
                        targetReturnTime = java.time.LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse returnTime: {}", returnTime, e);
            }
        }
        SeatClass.SeatClassType targetSeatClassType = null;
        if (seatClassType != null && !seatClassType.isEmpty()) {
            switch (seatClassType) {
                case "이코노미": targetSeatClassType = SeatClass.SeatClassType.ECONOMY; break;
                case "프리미엄 이코노미": targetSeatClassType = SeatClass.SeatClassType.PREMIUMECONOMY; break;
                case "비즈니스": targetSeatClassType = SeatClass.SeatClassType.BUSINESS; break;
                case "퍼스트": targetSeatClassType = SeatClass.SeatClassType.FIRST; break;
                default:
                    try {
                        targetSeatClassType = SeatClass.SeatClassType.valueOf(seatClassType.toUpperCase());
                    } catch (IllegalArgumentException ignored) { }
            }
        }
        for (Product p : calcProducts) {
            LocalDateTime actualDepartDateTime = null;
            LocalDateTime actualReturnArriveDateTime = null;
            if (p.getTour() != null && p.getTour().getSchedules() != null) {
                for (Schedule schedule : p.getTour().getSchedules()) {
                    if (schedule != null && Objects.equals(schedule.getDay(), 0L) && schedule.getLocations() != null) {
                        for (Location location : schedule.getLocations()) {
                            if (location != null && location.getLocationType() == LocationType.AIR
                                    && location.getSeatClass() != null && location.getSeatClass().getAir() != null) {
                                actualDepartDateTime = location.getSeatClass().getAir().getDepartDateTime();
                                break;
                            }
                        }
                        if (actualDepartDateTime != null) break;
                    }
                }
                Long lastDay = p.getTour().getSchedules().stream()
                        .filter(s -> s != null && s.getDay() != null)
                        .mapToLong(s -> s.getDay())
                        .max()
                        .orElse(0L);
                for (Schedule schedule : p.getTour().getSchedules()) {
                    if (schedule != null && schedule.getDay() != null && schedule.getDay().equals(lastDay)
                            && schedule.getLocations() != null) {
                        for (Location location : schedule.getLocations()) {
                            if (location != null && location.getLocationType() == LocationType.AIR
                                    && location.getSeatClass() != null && location.getSeatClass().getAir() != null) {
                                actualReturnArriveDateTime = location.getSeatClass().getAir().getArriveDateTime();
                                break;
                            }
                        }
                        if (actualReturnArriveDateTime != null) break;
                    }
                }
            }
            if (actualDepartDateTime == null) actualDepartDateTime = p.getDepartDateTime();
            if (actualReturnArriveDateTime == null) actualReturnArriveDateTime = p.getReturnDateTime();
            if (actualDepartDateTime == null) continue;
            java.time.LocalTime pDepartTime = actualDepartDateTime.toLocalTime();
            boolean departTimeMatch = pDepartTime.getHour() == targetDepartTime.getHour()
                    && pDepartTime.getMinute() == targetDepartTime.getMinute();
            if (!departTimeMatch) continue;
            boolean returnTimeMatch = true;
            if (targetReturnTime != null && actualReturnArriveDateTime != null) {
                java.time.LocalTime pReturnTime = actualReturnArriveDateTime.toLocalTime();
                returnTimeMatch = pReturnTime.getHour() == targetReturnTime.getHour()
                        && pReturnTime.getMinute() == targetReturnTime.getMinute();
            }
            boolean seatClassMatch = true;
            if (targetSeatClassType != null && p.getTour() != null && p.getTour().getSchedules() != null) {
                seatClassMatch = false;
                for (Schedule schedule : p.getTour().getSchedules()) {
                    if (schedule != null && schedule.getLocations() != null) {
                        for (Location location : schedule.getLocations()) {
                            if (location != null && location.getLocationType() == LocationType.AIR
                                    && location.getSeatClass() != null
                                    && location.getSeatClass().getClassType() == targetSeatClassType) {
                                seatClassMatch = true;
                                break;
                            }
                        }
                        if (seatClassMatch) break;
                    }
                }
            }
            if (returnTimeMatch && seatClassMatch) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * 카르테시안 곱으로 모든 조합 생성
     */
    private List<List<SeatClassAssignment>> generateCartesianProduct(List<AirLocationInfo> airLocationInfos) {
        List<List<SeatClassAssignment>> results = new ArrayList<>();
        
        if (airLocationInfos.isEmpty()) {
            return results;
        }
        
        generateCartesianProductRecursive(airLocationInfos, 0, new ArrayList<>(), results);
        
        return results;
    }
    
    /**
     * 재귀적으로 카르테시안 곱 생성 (시간 제약 검증 포함)
     */
    private void generateCartesianProductRecursive(
            List<AirLocationInfo> airLocationInfos,
            int index,
            List<SeatClassAssignment> current,
            List<List<SeatClassAssignment>> results) {
        
        if (index >= airLocationInfos.size()) {
            results.add(new ArrayList<>(current));
            return;
        }
        
        AirLocationInfo info = airLocationInfos.get(index);
        for (SeatClass seatClass : info.seatClasses) {
            // 같은 day의 이전 항공편이 있으면 시간 제약 확인
            if (!isValidTimeConstraint(info.day, seatClass, current)) {
                continue; // 시간 제약을 만족하지 않으면 스킵
            }
            
            SeatClassAssignment assignment = new SeatClassAssignment(
                info.day,
                info.departAirport,
                info.arriveAirport,
                seatClass
            );
            current.add(assignment);
            generateCartesianProductRecursive(airLocationInfos, index + 1, current, results);
            current.remove(current.size() - 1);
        }
    }
    
    /**
     * 시간 제약 검증: 같은 day의 이전 항공편 도착 시간 이후에 출발하는지 확인
     * @param day 현재 항공편의 day
     * @param seatClass 현재 선택하려는 항공편
     * @param current 이미 선택된 항공편 목록
     * @return 시간 제약을 만족하면 true
     */
    private boolean isValidTimeConstraint(Long day, SeatClass seatClass, List<SeatClassAssignment> current) {
        // 같은 day의 이전 항공편 찾기
        SeatClassAssignment previousFlight = null;
        for (int i = current.size() - 1; i >= 0; i--) {
            SeatClassAssignment assignment = current.get(i);
            if (Objects.equals(assignment.day, day)) {
                previousFlight = assignment;
                break;
            }
        }
        
        // 같은 day의 이전 항공편이 없으면 제약 없음
        if (previousFlight == null) {
            return true;
        }
        
        // 이전 항공편의 도착 시간
        LocalDateTime previousArriveTime = previousFlight.seatClass.getAir().getArriveDateTime();
        // 현재 항공편의 출발 시간
        LocalDateTime currentDepartTime = seatClass.getAir().getDepartDateTime();
        
        // 이전 항공편 도착 시간 이후에 출발해야 함 (최소 30분 여유)
        // 경유 시간을 고려하여 최소 30분 간격 필요
        LocalDateTime minDepartTime = previousArriveTime.plusMinutes(30);
        
        return currentDepartTime.isAfter(minDepartTime) || currentDepartTime.isEqual(minDepartTime);
    }
    
    /**
     * Product의 고유 키 생성 (출국편 + 귀국편 조합 + 출국 시간 + 출국 도착 시간 + 귀국 시간 + 귀국 도착 시간)
     * 같은 항공편 조합에 대해 중복 제거를 위해 사용
     * 개선: 출국편만 다른 상품이 절대 합쳐지지 않도록 도착 시간도 포함
     */
    /**
     * 🔧 달력용 키 생성: 출국 airId + 귀국 airId (좌석등급 무시)
     * 같은 항공편 조합에 대해 최저가 1개만 선택하기 위한 키
     */
    /**
     * 하단 리스트용 Product 키 생성
     * 키: 출국 airId + 귀국 airId + 좌석등급 + 출국시간 + 귀국시간
     * 같은 항공조합 + 같은 좌석등급 + 같은 시간 = 1개만 유지
     */
    private String generateListProductKey(Product product) {
        if (product == null || product.getTour() == null) {
            return null;
        }
        
        Hibernate.initialize(product.getTour().getSchedules());
        if (product.getTour().getSchedules() == null) {
            return null;
        }
        
        // 마지막 day 계산 (귀국 판단용)
        Long lastDay = product.getTour().getSchedules().stream()
            .filter(Objects::nonNull)
            .mapToLong(s -> s.getDay() != null ? s.getDay() : 0L)
            .max()
            .orElse(0L);
        
        Long departAirId = null;
        Long returnAirId = null;
        String departDateTime = null;
        String returnDateTime = null;
        SeatClass.SeatClassType seatClassType = null;
        
        // 모든 Schedule을 순회하여 출국편과 귀국편의 airId 찾기
        for (Schedule schedule : product.getTour().getSchedules()) {
            if (schedule == null) {
                continue;
            }
            Hibernate.initialize(schedule.getLocations());
            if (schedule.getLocations() == null) {
                continue;
            }
            
            for (Location location : schedule.getLocations()) {
                if (location != null && location.getLocationType() == LocationType.AIR && location.getSeatClass() != null) {
                    Hibernate.initialize(location.getSeatClass());
                    if (location.getSeatClass().getAir() != null) {
                        Hibernate.initialize(location.getSeatClass().getAir());
                        Air air = location.getSeatClass().getAir();
                        
                        SeatClass seatClass = location.getSeatClass();
                        if (seatClassType == null && seatClass != null) {
                            seatClassType = seatClass.getClassType();
                        }
                        
                        if (Objects.equals(schedule.getDay(), 0L)) {
                            // 출국일 (day 0)
                            departAirId = air.getId();
                            if (air.getDepartDateTime() != null) {
                                departDateTime = air.getDepartDateTime().toString();
                            }
                        } else if (Objects.equals(schedule.getDay(), lastDay)) {
                            // 귀국일 (마지막 day)
                            returnAirId = air.getId();
                            if (air.getArriveDateTime() != null) {
                                returnDateTime = air.getArriveDateTime().toString();
                            }
                        }
                    }
                }
            }
        }
        
        if (departAirId != null && returnAirId != null && seatClassType != null) {
            // 키: 출국 airId + 귀국 airId + 좌석등급 + 출국시간 + 귀국시간
            return departAirId + "_" + returnAirId + "_" + seatClassType.name() + "_" 
                + (departDateTime != null ? departDateTime : "") + "_" 
                + (returnDateTime != null ? returnDateTime : "");
        }
        
        return null;
    }
    
    private String calendarKey(Product product) {
        if (product == null || product.getTour() == null) {
            return null;
        }
        
        Hibernate.initialize(product.getTour().getSchedules());
        if (product.getTour().getSchedules() == null) {
            return null;
        }
        
        // 마지막 day 계산 (귀국 판단용)
        Long lastDay = product.getTour().getSchedules().stream()
            .filter(Objects::nonNull)
            .mapToLong(s -> s.getDay() != null ? s.getDay() : 0L)
            .max()
            .orElse(0L);
        
        Long departAirId = null;
        Long returnAirId = null;
        
        // 모든 Schedule을 순회하여 출국편과 귀국편의 airId 찾기
        for (Schedule schedule : product.getTour().getSchedules()) {
            if (schedule == null) {
                continue;
            }
            Hibernate.initialize(schedule.getLocations());
            if (schedule.getLocations() == null) {
                continue;
            }
            
            for (Location location : schedule.getLocations()) {
                if (location != null && location.getLocationType() == LocationType.AIR && location.getSeatClass() != null) {
                    Hibernate.initialize(location.getSeatClass());
                    if (location.getSeatClass().getAir() != null) {
                        Hibernate.initialize(location.getSeatClass().getAir());
                        Air air = location.getSeatClass().getAir();
                        
                        if (Objects.equals(schedule.getDay(), 0L)) {
                            // 출국일 (day 0)
                            departAirId = air.getId();
                        } else if (Objects.equals(schedule.getDay(), lastDay)) {
                            // 귀국일 (마지막 day)
                            returnAirId = air.getId();
                        }
                    }
                }
            }
        }
        
        if (departAirId != null && returnAirId != null) {
            return departAirId + "_" + returnAirId;
        }
        return null;
    }

    @GetMapping("/detail/{id}")
    public String getProductDetail(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departDate,
            @RequestParam(required = false) String departTime,
            @RequestParam(required = false) String returnTime,
            @RequestParam(required = false) String seatClassType,
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
            ProductCombinationResult combinationResult = findMultipleProductsForDate(productCopy, departDate, seatClassRepo);
            List<Product> calcProducts = combinationResult.getListProducts(); // 하단 리스트용 (모든 상품)
            
            // departTime을 LocalTime으로 파싱 (형식: "08:40:00" 또는 "08:40")
            java.time.LocalTime targetDepartTime = null;
            try {
                if (departTime.length() >= 5) {
                    String timeStr = departTime.substring(0, 5); // "HH:mm" 형식
                    String[] parts = timeStr.split(":");
                    if (parts.length == 2) {
                        targetDepartTime = java.time.LocalTime.of(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1])
                        );
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse departTime: {}", departTime, e);
            }
            
            // returnTime도 파싱 (귀국 시간으로 정확한 조합 매칭)
            java.time.LocalTime targetReturnTime = null;
            if (returnTime != null && !returnTime.isEmpty()) {
                try {
                    if (returnTime.length() >= 5) {
                        String timeStr = returnTime.substring(0, 5); // "HH:mm" 형식
                        String[] parts = timeStr.split(":");
                        if (parts.length == 2) {
                            targetReturnTime = java.time.LocalTime.of(
                                Integer.parseInt(parts[0]),
                                Integer.parseInt(parts[1])
                            );
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse returnTime: {}", returnTime, e);
                }
            }
            
            // 좌석 등급을 SeatClassType enum으로 변환
            SeatClass.SeatClassType targetSeatClassType = null;
            if (seatClassType != null && !seatClassType.isEmpty()) {
                // 한국어 좌석 등급명을 enum으로 변환
                switch (seatClassType) {
                    case "이코노미":
                        targetSeatClassType = SeatClass.SeatClassType.ECONOMY;
                        break;
                    case "프리미엄 이코노미":
                        targetSeatClassType = SeatClass.SeatClassType.PREMIUMECONOMY;
                        break;
                    case "비즈니스":
                        targetSeatClassType = SeatClass.SeatClassType.BUSINESS;
                        break;
                    case "퍼스트":
                        targetSeatClassType = SeatClass.SeatClassType.FIRST;
                        break;
                    default:
                        // 영어로 된 경우 직접 변환 시도
                        try {
                            targetSeatClassType = SeatClass.SeatClassType.valueOf(seatClassType.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            log.warn("Unknown seatClassType: {}", seatClassType);
                        }
                }
            }
            
            // 정확히 일치하는 항공편 조합 찾기
            // 출국 시간 + 귀국 시간 + 좌석 등급으로 정확히 매칭하여 달력에서 표시된 것과 동일한 조합 선택
            if (targetDepartTime != null) {
                for (Product p : calcProducts) {
                    // 실제 항공편의 출발 시간 가져오기 (day 0의 첫 번째 AIR Location)
                    LocalDateTime actualDepartDateTime = null;
                    LocalDateTime actualReturnArriveDateTime = null;
                    
                    if (p.getTour() != null && p.getTour().getSchedules() != null) {
                        for (Schedule schedule : p.getTour().getSchedules()) {
                            if (schedule != null && Objects.equals(schedule.getDay(), 0L)
                                && schedule.getLocations() != null) {
                                for (Location location : schedule.getLocations()) {
                                    if (location != null && location.getLocationType() == LocationType.AIR
                                        && location.getSeatClass() != null
                                        && location.getSeatClass().getAir() != null) {
                                        actualDepartDateTime = location.getSeatClass().getAir().getDepartDateTime();
                                        break;
                                    }
                                }
                                if (actualDepartDateTime != null) {
                                    break;
                                }
                            }
                        }
                        
                        // 마지막 day의 첫 번째 AIR Location에서 귀국편 도착 시간 가져오기
                        Long lastDay = p.getTour().getSchedules().stream()
                            .filter(s -> s != null && s.getDay() != null)
                            .mapToLong(s -> s.getDay())
                            .max()
                            .orElse(0L);
                        
                        for (Schedule schedule : p.getTour().getSchedules()) {
                            if (schedule != null && schedule.getDay() != null && schedule.getDay().equals(lastDay)
                                && schedule.getLocations() != null) {
                                for (Location location : schedule.getLocations()) {
                                    if (location != null && location.getLocationType() == LocationType.AIR
                                        && location.getSeatClass() != null
                                        && location.getSeatClass().getAir() != null) {
                                        actualReturnArriveDateTime = location.getSeatClass().getAir().getArriveDateTime();
                                        break;
                                    }
                                }
                                if (actualReturnArriveDateTime != null) {
                                    break;
                                }
                            }
                        }
                    }
                    
                    // 실제 항공편 시간을 찾지 못한 경우 fallback
                    if (actualDepartDateTime == null) {
                        actualDepartDateTime = p.getDepartDateTime();
                    }
                    if (actualReturnArriveDateTime == null) {
                        actualReturnArriveDateTime = p.getReturnDateTime();
                    }
                    
                    if (actualDepartDateTime != null) {
                        java.time.LocalTime pDepartTime = actualDepartDateTime.toLocalTime();
                        // 출국 시간 비교
                        boolean departTimeMatch = pDepartTime.getHour() == targetDepartTime.getHour() 
                            && pDepartTime.getMinute() == targetDepartTime.getMinute();
                        
                        if (departTimeMatch) {
                            // returnTime이 전달된 경우, 귀국 도착 시간도 비교
                            boolean returnTimeMatch = true;
                            if (targetReturnTime != null && actualReturnArriveDateTime != null) {
                                java.time.LocalTime pReturnTime = actualReturnArriveDateTime.toLocalTime();
                                returnTimeMatch = pReturnTime.getHour() == targetReturnTime.getHour() 
                                    && pReturnTime.getMinute() == targetReturnTime.getMinute();
                            }
                            
                            // 좌석 등급 비교
                            boolean seatClassMatch = true;
                            if (targetSeatClassType != null && p.getTour() != null && p.getTour().getSchedules() != null) {
                                seatClassMatch = false;
                                for (Schedule schedule : p.getTour().getSchedules()) {
                                    if (schedule != null && schedule.getLocations() != null) {
                                        for (Location location : schedule.getLocations()) {
                                            if (location != null && location.getLocationType() == LocationType.AIR
                                                && location.getSeatClass() != null
                                                && location.getSeatClass().getClassType() == targetSeatClassType) {
                                                seatClassMatch = true;
                                                break;
                                            }
                                        }
                                        if (seatClassMatch) {
                                            break;
                                        }
                                    }
                                }
                            }
                            
                            // 출국 시간 + 귀국 도착 시간 + 좌석 등급이 모두 일치하는 조합 선택
                            if (returnTimeMatch && seatClassMatch) {
                                calcProduct = p;
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        // 출발 시간으로 찾지 못한 경우, 같은 날짜의 모든 항공편 중 첫 번째 항공편 사용
        if (calcProduct == null) {
            ProductCombinationResult combinationResult = findMultipleProductsForDate(productCopy, departDate, seatClassRepo);
            List<Product> calcProducts = combinationResult.getListProducts(); // 하단 리스트용 (모든 상품)
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
            calcProduct.setReturnDateTime(departDateTime.plusDays(Math.max(1, itineraryDays - 1)));

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
        // ProductDetailDto 생성 전에 필요한 엔티티 초기화 (출국편/귀국편 정보 추출을 위해)
        if (calcProduct.getTour() != null) {
            Hibernate.initialize(calcProduct.getTour().getSchedules());
            if (calcProduct.getTour().getSchedules() != null) {
                for (Schedule schedule : calcProduct.getTour().getSchedules()) {
                    if (schedule != null) {
                        Hibernate.initialize(schedule.getLocations());
                        if (schedule.getLocations() != null) {
                            for (Location location : schedule.getLocations()) {
                                if (location != null && location.getLocationType() == LocationType.AIR
                                    && location.getSeatClass() != null) {
                                    Hibernate.initialize(location.getSeatClass());
                                    if (location.getSeatClass().getAir() != null) {
                                        Hibernate.initialize(location.getSeatClass().getAir());
                                        if (location.getSeatClass().getAir().getAirline() != null) {
                                            Hibernate.initialize(location.getSeatClass().getAir().getAirline());
                                        }
                                        if (location.getSeatClass().getAir().getDepartAirport() != null) {
                                            Hibernate.initialize(location.getSeatClass().getAir().getDepartAirport());
                                        }
                                        if (location.getSeatClass().getAir().getArriveAirport() != null) {
                                            Hibernate.initialize(location.getSeatClass().getAir().getArriveAirport());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 🔧 디버깅: 이미지 경로 확인
        log.info("[ProductController] 🔍 상품 상세 이미지 경로 - Product ID: {}, image: {}, thumbnail: {}, photos: {}", 
            id, 
            calcProduct.getImage(), 
            calcProduct.getThumbnail(),
            calcProduct.getPhotos() != null ? calcProduct.getPhotos().size() : 0);
        
        ProductDetailDto productDto = new ProductDetailDto(calcProduct);
        productDto.setReviews(reviews);
        
        // 🔧 디버깅: DTO의 이미지 경로 확인
        log.info("[ProductController] 🔍 ProductDetailDto 이미지 경로 - image: {}", productDto.getImage());

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
        model.addAttribute("departTime", departTime); // 예약 복구 시 동일 조합용
        model.addAttribute("returnTime", returnTime);
        model.addAttribute("seatClassType", seatClassType);

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

    // 최근 본 상품 단일 삭제
    @DeleteMapping("/wish/recent/{productId}")
    @ResponseBody
    @Transactional
    public Map<String, Boolean> removeRecentProduct(
            @PathVariable Long productId,
            Principal principal) {
        
        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        user = userRepo.findById(user.getId()).orElseThrow();
        
        List<RecentViewedItem> currentList = new ArrayList<>(user.getRecentProducts());
        boolean removed = currentList.removeIf(item -> item.getProductId().equals(productId));
        
        user.setRecentProducts(currentList);
        userRepo.saveAndFlush(user);
        
        return Map.of("success", removed);
    }
    
    // 찜한 상품 단일 삭제
    @DeleteMapping("/wish/liked/{productId}")
    @ResponseBody
    @Transactional
    public Map<String, Boolean> removeLikedProduct(
            @PathVariable Long productId,
            Principal principal) {
        
        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        user = userRepo.findById(user.getId()).orElseThrow();
        
        List<RecentViewedItem> currentList = new ArrayList<>(user.getLikedProducts());
        boolean removed = currentList.removeIf(item -> item.getProductId().equals(productId));
        
        user.setLikedProducts(currentList);
        userRepo.saveAndFlush(user);
        
        return Map.of("success", removed);
    }
    
    // 최근 본 상품 전체 삭제
    @DeleteMapping("/wish/recent/all")
    @ResponseBody
    @Transactional
    public Map<String, Boolean> clearRecentProducts(Principal principal) {
        
        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        user = userRepo.findById(user.getId()).orElseThrow();
        
        user.setRecentProducts(new ArrayList<>());
        userRepo.saveAndFlush(user);
        
        return Map.of("success", true);
    }
    
    // 찜한 상품 전체 삭제
    @DeleteMapping("/wish/liked/all")
    @ResponseBody
    @Transactional
    public Map<String, Boolean> clearLikedProducts(Principal principal) {
        
        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        user = userRepo.findById(user.getId()).orElseThrow();
        
        user.setLikedProducts(new ArrayList<>());
        userRepo.saveAndFlush(user);
        
        return Map.of("success", true);
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
                    String departTime = reservationData.get("departTime") != null ? reservationData.get("departTime").toString() : null;
                    String returnTime = reservationData.get("returnTime") != null ? reservationData.get("returnTime").toString() : null;
                    String seatClassType = reservationData.get("seatClassType") != null ? reservationData.get("seatClassType").toString() : null;

                    Product target = productRepo.findById(productId).orElseThrow();
                    if (departTime != null && !departTime.isEmpty()) {
                        Product productCopy = (Product) target.clone();
                        Hibernate.initialize(productCopy.getTour());
                        ProductCombinationResult combinationResult = findMultipleProductsForDate(productCopy, departDate, seatClassRepo);
                        List<Product> listProducts = combinationResult.getListProducts();
                        Product matched = findMatchingCalcProduct(listProducts, departTime, returnTime, seatClassType);
                        calcedProduct = matched != null ? matched : productServiceCommon.calcSingleProduct(target, departDate);
                    } else {
                        calcedProduct = productServiceCommon.calcSingleProduct(target, departDate);
                    }
                    session.setAttribute("calcProduct", calcedProduct);
                    session.setAttribute("departDate", departDate);
                } catch (Exception e) {
                    log.warn("reservationData 복구 실패", e);
                    model.addAttribute("error", "상품 정보를 찾을 수 없습니다.");
                    return "fragments/error/dataUnavailable";
                }
            }
        }

        if (calcedProduct == null) {
            model.addAttribute("error", "상품 정보를 확인할 수 없습니다. 상품 상세에서 다시 선택해 주세요.");
            return "fragments/error/dataUnavailable";
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
        if (calcedProduct == null) {
            model.addAttribute("error", "세션이 만료되었거나 상품 정보를 찾을 수 없습니다. 상품 상세에서 다시 선택해 주세요.");
            return "fragments/error/dataUnavailable";
        }

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
        int cutoff = (calcedProduct.getCutoffDays() != null) ? calcedProduct.getCutoffDays().intValue() : 5;
        purchaseProduct.setPaymentDueDate(calcedProduct.getDepartDateTime().minusDays(cutoff));
        purchaseProduct.setPassengerInfoDeadline(calcedProduct.getDepartDateTime().minusDays(cutoff));

        // PassengerDto 리스트로부터 PassengerProduct 엔티티 리스트 생성
        List<PassengerProduct> passengers = passengerServiceCommon.createPassengersFromDto(request.getPassengers());

        purchaseProduct.setPassengers(passengers);

        purchaseProductRepo.save(purchaseProduct);

        // 패키지 첫 예약 시 좌석 홀드 생성 (가능 인원만큼 출국/귀국 미리 확보)
        productServiceCommon.ensurePackageSeatHold(purchaseProduct);

        // PurchaseDetail 반환
        model.addAttribute("purchaseProduct", purchaseProduct);

        return "fragments/purchase/purchaseProductDetail";
    }

    @GetMapping("/purchase/{id}")
    String getPurchaseDetail(@PathVariable Long id, Principal principal, Model model) {

        PurchaseProduct purchaseProduct = purchaseProductRepo.findByIdWithAll(id).orElse(null);
        if (purchaseProduct == null) {
            return "fragments/error/dataUnavailable";
        }

        // 본인의 예약인지 확인
        if (principal != null) {
            User user = userRepo.findByEmail(principal.getName()).orElse(null);
            if (user != null && !purchaseProduct.getUser().getId().equals(user.getId())) {
                return "fragments/error/dataUnavailable"; // 다른 사용자의 예약 접근 시 에러
            }
        } else {
            return "fragments/error/dataUnavailable"; // 로그인하지 않은 사용자 접근 시 에러
        }

        model.addAttribute("purchaseProduct", purchaseProduct);

        return "fragments/purchase/purchaseProductDetail";
    }

    @GetMapping("/purchase/{id}/payment")
    String getPurchasePayForm(@PathVariable Long id, Model model, Principal principal) {

        if (principal == null) {
            model.addAttribute("error", "로그인이 필요합니다.");
            return "fragments/error/dataUnavailable";
        }
        PurchaseProduct purchaseProduct = purchaseProductRepo.findByIdWithAll(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다: " + id));
        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
        if (!purchaseProduct.getUser().getId().equals(user.getId())) {
            model.addAttribute("error", "본인의 예약만 결제할 수 있습니다.");
            return "fragments/error/dataUnavailable";
        }

        model.addAttribute("purchaseBase", purchaseProduct);
        model.addAttribute("paymentType", "product");

        return "fragments/payment";
    }

    @Transactional
    @PostMapping("/purchase/{id}/payment")
    ResponseEntity<?> postPurchasePayForm(
            @PathVariable Long id,
            @RequestBody PaymentRequest request,
            Principal principal,
            Model model) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        PurchaseProduct purchaseProduct = purchaseProductRepo.findByIdWithAll(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다: " + id));
        User buyer = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
        if (!purchaseProduct.getUser().getId().equals(buyer.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "본인의 예약만 결제할 수 있습니다."));
        }

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

        // 패키지 홀드가 있으면 allocated만 증가 (이미 홀드 시 reserveSeats 했으므로 추가 차감 없음)
        if (productServiceCommon.findPackageSeatHold(purchaseProduct).isPresent()) {
            productServiceCommon.allocatePackageSeatHold(purchaseProduct);
        } else {
            // 홀드 없음: 패키지 결제 확정 시 출국/귀국 좌석 둘 다 차감 (동일 인원 N으로 각 구간 차감)
            List<ConfirmedSeatClass> finalSeatClasses = purchaseProduct.getFinalSeatClasses();
            if (finalSeatClasses != null && !finalSeatClasses.isEmpty()) {
                for (ConfirmedSeatClass c : finalSeatClasses) {
                    if (c.getAirId() == null || c.getClassType() == null) {
                        continue;
                    }
                    long n = (c.getSeatCountAdult() != null ? c.getSeatCountAdult() : 0L)
                            + (c.getSeatCountYouth() != null ? c.getSeatCountYouth() : 0L);
                    if (n <= 0) {
                        continue;
                    }
                    seatClassRepo.findByAirIdAndClassType(c.getAirId(), c.getClassType())
                            .ifPresent(seatClass -> {
                                try {
                                    seatClass.reserveSeats(n);
                                    seatClassRepo.save(seatClass);
                                } catch (Exception e) {
                                    throw new RuntimeException("좌석 차감 실패: " + e.getMessage(), e);
                                }
                            });
                }
            }
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/purchase/{id}/inquiry")
    String getInquiryForm(@PathVariable Long id, Model model, Principal principal) {

        if (principal == null) {
            model.addAttribute("error", "로그인이 필요합니다.");
            return "fragments/error/dataUnavailable";
        }
        PurchaseProduct purchaseProduct = purchaseProductRepo.findByIdWithAll(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다: " + id));
        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
        if (!purchaseProduct.getUser().getId().equals(user.getId())) {
            model.addAttribute("error", "본인의 예약만 문의할 수 있습니다.");
            return "fragments/error/dataUnavailable";
        }

        model.addAttribute("purchase", purchaseProduct);

        return "fragments/inquiry/inquiryForm";
    }

    @PostMapping("/purchase/{id}/inquiry")
    ResponseEntity<?> postInquiryForm(
            @PathVariable Long id,
            @RequestBody InquiryRequestDto request,
            Principal principal,
            Model model) {

        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
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
            // 결제 전 예약 취소: cancelPurchase 호출로 홀드 감소 + 대기열 처리
            productServiceCommon.cancelPurchase(id);
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
