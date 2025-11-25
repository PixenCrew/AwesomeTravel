package renewal.awesome_travel.product.service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
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

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.product.dto.ProductSearchRequestDto;
import renewal.awesome_travel.product.dto.ProductSpecification;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.common.entity.MenuCode;
import renewal.common.entity.Product;
import renewal.common.entity.User;
import renewal.common.entity.User.RecentViewedItem;
import renewal.common.repository.ProductRepository;
import renewal.common.service.ProductServiceCommon;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final ProductServiceCommon productServiceCommon;

    public List<Product> searchProducts(ProductSearchRequestDto filter) {
        Specification<Product> spec = Specification.where(null);

        if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
            String normalized = Normalizer.normalize(filter.getKeyword().trim(), Normalizer.Form.NFC);

            spec = spec.and(ProductSpecification.keywordContains(normalized));
        }
        return productRepo.findAll(spec);
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
            Product calcProduct = productServiceCommon.calcSingleProduct(product, departDate);
            if (calcProduct != null) {
                availProducts.add(calcProduct);
            }
        }

        return availProducts;
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

    public void saveRecentViewToDB(User user, Long productId, LocalDateTime viewedAt) {

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

    public void saveRecentViewToCookie(HttpServletRequest request,
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

        // ID 목록 기준 Product 조회
        Map<Long, Product> productMap = productRepo.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 원래 RecentViewed 순서를 유지하여 리스트 생성
        return items.stream()
                .map(item -> {
                    Product p = productMap.get(item.getProductId());
                    if (p != null) {
                        p.setViewedAt(item.getViewedAt());
                    }
                    return p;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public boolean wished(User user, Long productId) {
        return user.getLikedProducts().stream().anyMatch(p -> p.getProductId().equals(productId));
    }

}
