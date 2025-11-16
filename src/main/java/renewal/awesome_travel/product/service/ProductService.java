package renewal.awesome_travel.product.service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.air.repository.SeatClassRepository;
import renewal.awesome_travel.product.dto.ProductSearchRequestDto;
import renewal.awesome_travel.product.dto.ProductSpecification;
import renewal.awesome_travel.product.repository.ProductRepository;
import renewal.awesome_travel.purchase.repository.PurchaseProductRepository;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.common.entity.AirportCode;
import renewal.common.entity.Location;
import renewal.common.entity.Location.LocationType;
import renewal.common.entity.MenuCode;
import renewal.common.entity.Product;
import renewal.common.entity.Product.DepartTimeType;
import renewal.common.entity.Product.ProductStatus;
import renewal.common.entity.PurchaseBase.PurchaseStatus;
import renewal.common.entity.PurchaseProduct;
import renewal.common.entity.Schedule;
import renewal.common.entity.SeatClass;
import renewal.common.entity.Tour;
import renewal.common.entity.User;
import renewal.common.entity.User.RecentViewedItem;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final PurchaseProductRepository purchaseProductRepo;
    private final SeatClassRepository seatClassRepo;
    private final UserRepository userRepo;

    public Page<Product> searchProducts(ProductSearchRequestDto filter, Pageable pageable) {
        Specification<Product> spec = Specification.where(null);

        if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
            spec = spec.and(ProductSpecification.keywordContains(filter.getKeyword()));
        }
        return productRepo.findAll(spec, pageable);
    }

    // private final ProductRepository productRepo;
    // private final AirRepository airRepository;
    // private final HotelRepository hotelRepository;
    // private final TagRepository tagRepository;

    // public Product createProduct(ProductRequestDto productRequestDto) {
    // List<Tag> tags = tagRepository.findAllById(productRequestDto.getTagIds());

    // Product savedProduct =
    // productRepo.save(ProductMapper.toProduct(productRequestDto, tags,
    // null));

    // List<ProductList> productLists = productRequestDto.getProductLists().stream()
    // .map(dto -> {
    // Air air = airRepository.findById(dto.getAir_id()).orElse(null);
    // Hotel hotel = hotelRepository.findById(dto.getHotel_id()).orElse(null);
    // return ProductListMapper.toProductList(dto, savedProduct, air, hotel);
    // })
    // .collect(Collectors.toList());

    // savedProduct.getProductLists().addAll(productLists);
    // return productRepo.save(savedProduct); // ProductList도 저장됨 (Cascade.ALL
    // 설정 시)
    // }

    public List<Product> calcProduct(List<Product> products, LocalDate departDate) {
        List<Product> availProducts = new ArrayList<>();

        for (Product product : products) {
            Product calcProduct = calcSingleProduct(product, departDate);
            if (calcProduct != null) {
                availProducts.add(calcProduct);
            }
        }

        return availProducts;
    }

    public Product calcSingleProduct(Product product, LocalDate departDate) {
        // System.out.println("\n=== calcSingleProduct 호출 ===");
        // System.out.println("target.id = " + product.getId());
        // System.out.println("target.name = " + product.getTitle());
        // System.out.println("departDate = " + departDate);

        Hibernate.initialize(product.getTour());
        Tour tour = product.getTour();

        // 초기 가격 계산
        Long finalPriceAdult = tour.getPriceAdult();
        Long finalPriceYouth = tour.getPriceYouth();
        Long finalPriceInfant = tour.getPriceInfant();

        List<Schedule> schedules = tour.getSchedules();

        for (Schedule sced : schedules) {
            List<Location> locations = sced.getLocations();
            for (Location loc : locations) {
                LocationType type = loc.getLocationType();
                if (type == LocationType.AIR) {

                    // // cutoffDays 적용 여부 선택
                    // LocalDate departDate = today;
                    // if (applyCutoff) {
                    // departDate = departDate.plusDays(product.getCutoffDays());
                    // }
                    // departDate = departDate.plusDays(sced.getDay());

                    LocalDate currentdepartDate = departDate.plusDays(sced.getDay());
                    DepartTimeType dtt = product.getDepartTimeType();
                    int startHour = sced.getDay() == 0 ? dtt.getStartHour() : 0;
                    int endHour = sced.getDay() == 0 ? dtt.getEndHour() : 23;

                    LocalDateTime startDateTime = currentdepartDate.atTime(startHour, 0);
                    LocalDateTime endDateTime = currentdepartDate.atTime(endHour, 59, 59);

                    AirportCode departAirport = loc.getDepartAirport();
                    AirportCode arriveAirport = loc.getArriveAirport();
                    // System.out.println("\n=======findLowestPriceSeat========");
                    // System.out.println(startDateTime);
                    // System.out.println(endDateTime);
                    // System.out.println(departAirport.getAirportCode());
                    // System.out.println(arriveAirport.getAirportCode());
                    // System.out.println(product.getSeatClassTypes());
                    // System.out.println("=======findLowestPriceSeat========\n");

                    SeatClass finalSeat = seatClassRepo.findLowestPriceSeat(
                            startDateTime,
                            endDateTime,
                            departAirport,
                            arriveAirport,
                            product.getSeatClassTypes());

                    // 항공권 없으면 null 반환
                    if (finalSeat == null) {
                        // System.out.println(
                        // "==================== null 반환: 해당 날짜(" + currentdepartDate
                        // + ")에 항공편 없음=====================");

                        return null;
                    }

                    loc.setSeatClass(finalSeat);

                    if (product.getDepartDateTime() == null) { // 첫 항공권 출발시간 (=출국시간)
                        product.setDepartDateTime(finalSeat.getAir().getDepartDateTime());
                    }

                    // 한 product에 대해 항공권 도착시간 계속 덮어씌움 => 마지막 항공권의 도착시간 (=귀국시간)
                    product.setReturnDateTime(finalSeat.getAir().getArriveDateTime());

                    // 항공권 잔여좌석 확인로직 -> 해당 날짜의 상품 예약자 수 확인로직으로 변경
                    // // 한 product에 대해 항공권 잔여좌석 낮은쪽 계속 덮어씌움 => 예약 가능인 수 저장
                    // if (product.getAvailableSeats() == null
                    // || product.getAvailableSeats() > finalSeat.getAvailableSeats()) {
                    // product.setAvailableSeats(finalSeat.getAvailableSeats());
                    // }

                    finalPriceAdult += finalSeat.getPriceAdult();
                    finalPriceYouth += finalSeat.getPriceYouth();
                    finalPriceInfant += finalSeat.getPriceInfant();

                } else if (type == LocationType.HOTEL) {
                    finalPriceAdult += loc.getHotel().getPrice();
                    finalPriceYouth += loc.getHotel().getPrice();
                    // 영유아는 호텔 포함 안함
                }
            }
        }

        product.setFinalPriceAdult(finalPriceAdult);
        product.setFinalPriceYouth(finalPriceYouth);
        product.setFinalPriceInfant(finalPriceInfant);

        // 항공권 잔여좌석 확인로직 -> 해당 날짜의 상품 예약자 수 확인로직으로 변경
        Long reserved = 0L;
        // 해당 날짜의 상품 예약들
        List<PurchaseProduct> purchaseProducts = purchaseProductRepo.findByProductAndDepartDate(product, departDate);
        for (PurchaseProduct pp : purchaseProducts) {
            if (pp.getPurchaseStatus() != PurchaseStatus.CANCELLED && !pp.isWaiting()) {
                reserved += pp.getAdultCount();
                reserved += pp.getYouthCount();
                // reserved += pp.getInfantCount(); // 영유아는 인원수 카운트 안함
            }

            // waiting인 주문이 하나라도 있으면 예약대기 상품임
            if (pp.isWaiting()) {
                product.setProductStatus(ProductStatus.WAITING);
            }
        }
        product.setReservedSeats(reserved);
        product.setAvailableSeats(product.getTour().getMaxCapacity() - reserved);

        return product;
    }

    public List<Product> findProductsByMenuCode(MenuCode menuCode) {
        Set<String> cityCodes = new HashSet<>();
        Set<String> countryCodes = new HashSet<>();
        Set<Long> productIds = new HashSet<>();

        // 모든 MenuCodeDetail을 분류
        for (MenuCode.MenuCodeDetail detail : menuCode.getDetails()) {
            switch (detail.getTargetColumn()) {
                case CITY:
                    cityCodes.add(detail.getValue());
                    break;
                case COUNTRY:
                    countryCodes.add(detail.getValue());
                    break;
                case ID:
                    productIds.add(Long.valueOf(detail.getValue()));
                    break;
            }
        }

        List<Product> result = new ArrayList<>();

        if (!cityCodes.isEmpty()) {
            result.addAll(productRepo.findDistinctByTour_Schedules_Locations_CityCode_CityCodeIn(cityCodes));
        }
        if (!countryCodes.isEmpty()) {
            result.addAll(productRepo.findAllByTour_Country_CountryCodeIn(countryCodes));
        }
        if (!productIds.isEmpty()) {
            result.addAll(productRepo.findAllById(productIds));
        }

        return result;
    }

    public void saveRecentView(
            Principal pricipal,
            HttpServletRequest request,
            HttpServletResponse response,
            Long productId,
            LocalDateTime viewedAt) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 로그인 상태인지 체크
        boolean isLoggedIn = auth != null &&
                auth.isAuthenticated() &&
                !"anonymousUser".equals(auth.getPrincipal());

        if (isLoggedIn) {
            // 로그인: DB 에 저장
            User user = userRepo.findByEmail(pricipal.getName()).get();
            saveRecentViewToDB(user, productId, viewedAt);
        } else {
            // 비로그인: 쿠키에 저장
            saveRecentViewToCookie(request, response, productId, viewedAt);
        }
    }

    private void saveRecentViewToDB(User user, Long productId, LocalDateTime viewedAt) {

        List<RecentViewedItem> list = user.getRecentProducts();

        // 기존 동일 상품 제거하여 중복 방지
        list.removeIf(item -> item.getProductId().equals(productId));

        // 맨 앞에 추가
        list.add(0, new RecentViewedItem(productId, viewedAt));

        // 최대 10개만 유지
        if (list.size() > 10)
            list = list.subList(0, 10);

        user.setRecentProducts(list);
        userRepo.save(user);
    }

    private void saveRecentViewToCookie(HttpServletRequest request,
            HttpServletResponse response,
            Long productId,
            LocalDateTime viewedAt) {
        ObjectMapper mapper = new ObjectMapper();
        String cookieName = "recent_view";

        // 1) 기존 쿠키 읽기
        String cookieValue = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (cookieName.equals(c.getName())) {
                    cookieValue = c.getValue();
                    break;
                }
            }
        }

        List<Map<String, Object>> list = new ArrayList<>();

        try {
            if (cookieValue != null) {
                list = mapper.readValue(URLDecoder.decode(cookieValue, StandardCharsets.UTF_8),
                        new TypeReference<List<Map<String, Object>>>() {
                        });
            }
        } catch (Exception ignored) {
        }

        // 2) 동일 상품 제거(중복 방지)
        list.removeIf(item -> item.get("id").equals(productId));

        // 3) 새 레코드 앞에 추가
        Map<String, Object> record = new HashMap<>();
        record.put("id", productId);
        record.put("t", viewedAt.toEpochSecond(ZoneOffset.UTC));
        list.add(0, record);

        // 4) 10개 제한
        if (list.size() > 10)
            list = list.subList(0, 10);

        // 5) JSON → 쿠키 저장
        try {
            String encodedJson = URLEncoder.encode(mapper.writeValueAsString(list), StandardCharsets.UTF_8);
            Cookie cookie = new Cookie(cookieName, encodedJson);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24 * 7); // 7일 보관
            response.addCookie(cookie);
        } catch (Exception ignored) {
        }
    }

    public List<RecentViewedItem> loadRecentViewProducts(HttpServletRequest request) {

        String cookieName = "recent_view";
        String cookieValue = null;

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (cookieName.equals(c.getName())) {
                    cookieValue = c.getValue();
                    break;
                }
            }
        }

        if (cookieValue == null || cookieValue.isBlank()) {
            return Collections.emptyList();
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> rawList;

        try {
            // 쿠키의 URL 인코딩 해제 후 JSON 파싱
            rawList = mapper.readValue(
                    URLDecoder.decode(cookieValue, StandardCharsets.UTF_8),
                    new TypeReference<List<Map<String, Object>>>() {
                    });
        } catch (Exception e) {
            return Collections.emptyList();
        }

        // JSON → RecentViewedItem 변환
        List<RecentViewedItem> items = rawList.stream()
                .map(m -> new RecentViewedItem(
                        ((Number) m.get("id")).longValue(),
                        LocalDateTime.ofEpochSecond(
                                ((Number) m.get("t")).longValue(),
                                0,
                                ZoneOffset.UTC)))
                .collect(Collectors.toList());

        // 시간순 정렬 (최근 본 순 → 오래된 순)
        items.sort((a, b) -> b.getViewedAt().compareTo(a.getViewedAt()));

        return items;
    }

    public List<Product> convertToProducts(List<RecentViewedItem> items) {
        if (items == null || items.isEmpty())
            return Collections.emptyList();

        List<Long> ids = items.stream()
                .map(RecentViewedItem::getProductId)
                .toList();

        // ID 목록 기준 Product 조회 (정렬 유지 가능)
        Map<Long, Product> productMap = productRepo.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 원래 RecentViewed 순서를 유지하여 리스트 생성
        return items.stream()
                .map(item -> {
                    Product p = productMap.get(item.getProductId());
                    if (p != null) {
                        p.setViewedAt(item.getViewedAt()); // ★ Transient 필드 주입
                    }
                    return p;
                })
                .filter(Objects::nonNull)
                .toList();
    }

}
