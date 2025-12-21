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

            // 같은 날짜의 모든 항공편 찾기
            List<Product> calcProducts = findMultipleProductsForDate(target, targetDate, seatClassRepo);
            
            // 각 항공편에 대해 ProductCalanderDto 생성
            for (Product calcProduct : calcProducts) {
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

        // 6개월치를 돌았는데도 항공권이 없는 경우
        boolean hasNoProducts = result.isEmpty();
        model.addAttribute("products", result);
        model.addAttribute("hasNoProducts", hasNoProducts);
        model.addAttribute("productTitle", target.getTitle());

        return "fragments/product/productCalander";
    }
    
    /**
     * 같은 날짜에 여러 항공편을 찾아서 각각에 대해 Product를 계산하여 반환
     * 모든 Schedule의 모든 AIR Location에 대해 모든 항공편 조합을 생성
     */
    private List<Product> findMultipleProductsForDate(Product product, LocalDate departDate, SeatClassRepository seatClassRepo) {
        List<Product> results = new ArrayList<>();
        
        try {
            Product productCopy = (Product) product.clone();
            Hibernate.initialize(productCopy.getTour());
            
            if (productCopy.getTour() == null || productCopy.getTour().getSchedules() == null) {
                return results;
            }
            
            // 모든 Schedule의 모든 AIR Location에 대해 항공편 찾기
            List<AirLocationInfo> airLocationInfos = new ArrayList<>();
            for (Schedule sced : productCopy.getTour().getSchedules()) {
                if (sced == null) {
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
                    
                    // 해당 Schedule의 날짜 계산
                    LocalDate currentScheduleDate = departDate.plusDays(sced.getDay());
                    LocalDateTime startDateTime = currentScheduleDate.atTime(0, 0);
                    LocalDateTime endDateTime = currentScheduleDate.atTime(23, 59, 59);
                    
                    // 같은 날짜의 모든 항공편 조회
                    List<SeatClass> allSeats = seatClassRepo.findLowestPriceSeatsByAirportCodes(
                        startDateTime, endDateTime,
                        departAirport.getAirportCode(),
                        arriveAirport.getAirportCode(),
                        productCopy.getSeatClassTypes());
                    
                    // 조합 생성 순서를 고정하기 위해 가격순으로 정렬 (같은 출국 시간에 대해 항상 같은 조합 선택)
                    allSeats.sort((a, b) -> {
                        int priceCompare = Long.compare(
                            a.getPriceAdult() != null ? a.getPriceAdult() : 0L,
                            b.getPriceAdult() != null ? b.getPriceAdult() : 0L
                        );
                        if (priceCompare != 0) {
                            return priceCompare;
                        }
                        // 가격이 같으면 출발 시간순으로 정렬
                        if (a.getAir() != null && b.getAir() != null) {
                            return a.getAir().getDepartDateTime().compareTo(b.getAir().getDepartDateTime());
                        }
                        return 0;
                    });
                    
                    if (!allSeats.isEmpty()) {
                        airLocationInfos.add(new AirLocationInfo(sced.getDay(), loc, departAirport, arriveAirport, allSeats));
                    }
                }
            }
            
            // 좌석 등급별로 그룹화하여 같은 등급끼리만 매칭
            // 각 좌석 등급(ECONOMY, BUSINESS 등)별로 별도의 조합 생성
            List<Product> tempProducts = new ArrayList<>();
            
            // 허용된 모든 좌석 등급에 대해 각각 조합 생성
            if (productCopy.getSeatClassTypes() != null && !productCopy.getSeatClassTypes().isEmpty()) {
                for (SeatClass.SeatClassType seatClassType : productCopy.getSeatClassTypes()) {
                    // 해당 좌석 등급만 필터링한 AirLocationInfo 리스트 생성
                    List<AirLocationInfo> filteredAirLocationInfos = new ArrayList<>();
                    for (AirLocationInfo info : airLocationInfos) {
                        List<SeatClass> filteredSeats = info.seatClasses.stream()
                            .filter(seat -> seat.getClassType() == seatClassType)
                            .collect(java.util.stream.Collectors.toList());
                        if (!filteredSeats.isEmpty()) {
                            filteredAirLocationInfos.add(new AirLocationInfo(
                                info.day, info.location, info.departAirport, info.arriveAirport, filteredSeats));
                        }
                    }
                    
                    // 해당 좌석 등급의 모든 조합 생성 (카르테시안 곱)
                    List<List<SeatClassAssignment>> combinations = generateCartesianProduct(filteredAirLocationInfos);
                    
                    // 출국일(day 0)과 귀국일(마지막 day)의 항공사가 같은 조합만 필터링
                    Long firstDay = filteredAirLocationInfos.stream()
                        .mapToLong(info -> info.day)
                        .min()
                        .orElse(0L);
                    Long lastDay = filteredAirLocationInfos.stream()
                        .mapToLong(info -> info.day)
                        .max()
                        .orElse(0L);
                    
                    List<List<SeatClassAssignment>> filteredCombinations = new ArrayList<>();
                    for (List<SeatClassAssignment> combination : combinations) {
                        // 출국일(day 0)의 항공사 찾기
                        SeatClassAssignment departAssignment = combination.stream()
                            .filter(a -> a.day.equals(firstDay))
                            .findFirst()
                            .orElse(null);
                        
                        // 귀국일(마지막 day)의 항공사 찾기
                        SeatClassAssignment returnAssignment = combination.stream()
                            .filter(a -> a.day.equals(lastDay))
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
                    
                    // 각 조합마다 Product 생성
                    for (List<SeatClassAssignment> combination : filteredCombinations) {
                        try {
                            Product productForCombination = (Product) product.clone();
                            
                            // 각 AIR Location에 SeatClass 설정
                            for (SeatClassAssignment assignment : combination) {
                                for (Schedule schedule : productForCombination.getTour().getSchedules()) {
                                    if (schedule.getDay() == assignment.day) {
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
                            if (calcProduct != null) {
                                tempProducts.add(calcProduct);
                            }
                        } catch (CloneNotSupportedException e) {
                            log.error("Product clone failed for combination", e);
                        }
                    }
                }
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
                        .filter(a -> a.day.equals(firstDay))
                        .findFirst()
                        .orElse(null);
                    
                    // 귀국일(마지막 day)의 항공사 찾기
                    SeatClassAssignment returnAssignment = combination.stream()
                        .filter(a -> a.day.equals(lastDay))
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
                
                for (List<SeatClassAssignment> combination : filteredCombinations) {
                    try {
                        Product productForCombination = (Product) product.clone();
                        
                        // 각 AIR Location에 SeatClass 설정
                        for (SeatClassAssignment assignment : combination) {
                            for (Schedule schedule : productForCombination.getTour().getSchedules()) {
                                if (schedule.getDay() == assignment.day) {
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
                        if (calcProduct != null) {
                            tempProducts.add(calcProduct);
                        }
                    } catch (CloneNotSupportedException e) {
                        log.error("Product clone failed for combination", e);
                    }
                }
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
            
            results.addAll(tempProducts);
        } catch (CloneNotSupportedException e) {
            log.error("Product clone failed", e);
        }
        
        return results;
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
            if (assignment.day.equals(day)) {
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

    @GetMapping("/detail/{id}")
    public String getProductDetail(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departDate,
            @RequestParam(required = false) String departTime,
            @RequestParam(required = false) String returnTime,
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
            
            // 정확히 일치하는 항공편 조합 찾기
            // 출국 시간 + 귀국 시간으로 정확히 매칭하여 달력에서 표시된 것과 동일한 조합 선택
            if (targetDepartTime != null) {
                for (Product p : calcProducts) {
                    if (p.getDepartDateTime() != null) {
                        java.time.LocalTime pDepartTime = p.getDepartDateTime().toLocalTime();
                        // 출국 시간 비교
                        boolean departTimeMatch = pDepartTime.getHour() == targetDepartTime.getHour() 
                            && pDepartTime.getMinute() == targetDepartTime.getMinute();
                        
                        if (departTimeMatch) {
                            // returnTime이 전달된 경우, 귀국 시간도 비교
                            if (targetReturnTime != null && p.getReturnDateTime() != null) {
                                java.time.LocalTime pReturnTime = p.getReturnDateTime().toLocalTime();
                                boolean returnTimeMatch = pReturnTime.getHour() == targetReturnTime.getHour() 
                                    && pReturnTime.getMinute() == targetReturnTime.getMinute();
                                if (returnTimeMatch) {
                                    calcProduct = p; // 출국 + 귀국 시간이 모두 일치하는 조합
                                    break;
                                }
                            } else {
                                // returnTime이 없으면 출국 시간만 일치하는 첫 번째 조합 선택
                                if (calcProduct == null) {
                                    calcProduct = p;
                                }
                            }
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
