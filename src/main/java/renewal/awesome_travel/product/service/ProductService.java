package renewal.awesome_travel.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.air.repository.SeatClassRepository;
import renewal.awesome_travel.product.dto.ProductSearchRequestDto;
import renewal.awesome_travel.product.dto.ProductSpecification;
import renewal.awesome_travel.product.repository.ProductRepository;
import renewal.common.entity.AirportCode;
import renewal.common.entity.Location;
import renewal.common.entity.Location.LocationType;
import renewal.common.entity.MenuCode;
import renewal.common.entity.Product;
import renewal.common.entity.Product.DepartTimeType;
import renewal.common.entity.Schedule;
import renewal.common.entity.SeatClass;
import renewal.common.entity.Tour;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final SeatClassRepository seatClassRepo;

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
        System.out.println("\n=== calcSingleProduct 호출 ===");
        System.out.println("target.id = " + product.getId());
        System.out.println("target.name = " + product.getTitle());
        System.out.println("departDate = " + departDate);

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

                    departDate = departDate.plusDays(sced.getDay());
                    DepartTimeType dtt = product.getDepartTimeType();
                    int startHour = sced.getDay() == 0 ? dtt.getStartHour() : 0;
                    int endHour = sced.getDay() == 0 ? dtt.getEndHour() : 23;

                    LocalDateTime startDateTime = departDate.atTime(startHour, 0);
                    LocalDateTime endDateTime = departDate.atTime(endHour, 59, 59);

                    AirportCode departAirport = loc.getDepartAirport();
                    AirportCode arriveAirport = loc.getArriveAirport();
                    System.out.println("\n=======findLowestPriceSeat========");
                    System.out.println(startDateTime);
                    System.out.println(endDateTime);
                    System.out.println(departAirport.getAirportCode());
                    System.out.println(arriveAirport.getAirportCode());
                    System.out.println(product.getSeatClassTypes());
                    System.out.println("=======findLowestPriceSeat========\n");

                    SeatClass finalSeat = seatClassRepo.findLowestPriceSeat(
                            startDateTime,
                            endDateTime,
                            departAirport,
                            arriveAirport,
                            product.getSeatClassTypes());

                    // 항공권 없으면 null 반환
                    if (finalSeat == null) {
                        System.out.println(
                                "==================== null 반환: 해당 날짜(" + departDate + ")에 항공편 없음=====================");

                        return null;
                    }

                    loc.setSeatClass(finalSeat);

                    if (product.getDepartDateTime() == null) { // 첫 항공권 출발시간 (=출국시간)
                        product.setDepartDateTime(finalSeat.getAir().getDepartDateTime());
                    }

                    // 한 product에 대해 항공권 도착시간 계속 덮어씌움 => 마지막 항공권의 도착시간 (=귀국시간)
                    product.setReturnDateTime(finalSeat.getAir().getArriveDateTime());

                    // TODO 항공권 잔여좌석 확인로직 -> 해당 날짜의 상품 예약자 수 확인로직으로 변경
                    // 한 product에 대해 항공권 잔여좌석 낮은쪽 계속 덮어씌움 => 예약 가능인 수 저장
                    if (product.getAvailableSeats() == null
                            || product.getAvailableSeats() > finalSeat.getAvailableSeats()) {
                        product.setAvailableSeats(finalSeat.getAvailableSeats());
                    }

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

}
