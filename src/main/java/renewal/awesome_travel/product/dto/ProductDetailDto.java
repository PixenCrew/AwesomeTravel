package renewal.awesome_travel.product.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.common.entity.Location;
import renewal.common.entity.Location.LocationType;
import renewal.common.entity.Product;
import renewal.common.entity.Product.Info;
import renewal.common.entity.Product.ProductStatus;
import renewal.common.entity.Review;
import renewal.common.entity.Schedule;
import renewal.common.entity.TimeDeal.DiscountType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailDto {

    // 기본정보
    private Long id;
    private Long availableSeats;

    // 표시 사진들
    private List<String> photos;

    // 제목섹션
    private String productCode;
    private String title;
    private LocalDateTime departDateTime;
    private Long priceAdult;
    private Long priceYouth;
    private Long priceInfant;
    
    // 가격 구성 요소 (디버깅용)
    private Long tourPrice; // 투어 기본 가격
    private Long totalAirPrice; // 항공권 총 가격
    private Long totalHotelPrice; // 호텔 총 가격
    private String priceBreakdown; // 가격 상세 내역 (텍스트)

    // 타임딜 정보
    private Long originalPriceAdult;
    private Long originalPriceYouth;
    private Long originalPriceInfant;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private DiscountType discountType;
    private Long discountValue;

    // 중간 정보 섹션
    private ProductStatus productStatus;
    private Long bak; // N박 숙박횟수
    private Long il; // M일
    private String airline; // 항공사 ( 출발항공사 이름 넣음 )
    private boolean noShopping; // 노쇼핑?
    private boolean noOption; // 노옵션?
    
    // 출국편 정보
    private LocalDateTime departFlightDateTime; // 출국편 출발 시간
    private LocalDateTime departFlightArriveDateTime; // 출국편 도착 시간
    private String departFlightNumber;
    private String departFlightAirline;
    private String departFlightDepartAirport;
    private String departFlightArriveAirport;
    
    // 귀국편 정보
    private LocalDateTime returnFlightDateTime; // 귀국편 출발 시간
    private LocalDateTime returnFlightArriveDateTime; // 귀국편 도착 시간
    private String returnFlightNumber;
    private String returnFlightAirline;
    private String returnFlightDepartAirport;
    private String returnFlightArriveAirport;

    // 상세.1 상품정보 섹션
    private String image;
    // Info = title, content, appendix
    private List<Info> included; // ex) 교통 : 왕복항공권 └ 세금포함 어쩌구 작은글씨
    private List<Info> excluded;
    // private List<Info> term; // 약관은 미리 만든 HTML 사용

    // 상세.2 일정표 섹션
    private List<Schedule> schedules;

    // 상세.3 리뷰 섹션
    private Long star1; // 1점 리뷰 수
    private Long star2; // 2점 리뷰 수
    private Long star3; // ...
    private Long star4;
    private Long star5;
    private List<Review> reviews;

    public ProductDetailDto(Product product) {

        this.id = product.getId();
        this.availableSeats = product.getAvailableSeats();

        this.photos = product.getPhotos();
        this.image = product.getImage();

        this.title = product.getTitle();
        this.departDateTime = product.getDepartDateTime();
        this.priceAdult = product.getFinalPriceAdult();
        this.priceYouth = product.getFinalPriceYouth();
        this.priceInfant = product.getFinalPriceInfant();

        this.productStatus = product.getProductStatus();
        this.airline = null;
        if (product.getTour() != null && product.getTour().getSchedules() != null) {
            this.airline = product.getTour().getSchedules().stream()
                    .filter(Objects::nonNull)
                    .flatMap(schedule -> schedule.getLocations().stream())
                    .filter(Objects::nonNull)
                    .map(location -> location.getSeatClass())
                    .filter(Objects::nonNull)
                    .map(seatClass -> seatClass.getAir())
                    .filter(Objects::nonNull)
                    .map(air -> air.getAirline())
                    .filter(Objects::nonNull)
                    .map(airlineEntity -> airlineEntity.getNameKor())
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        if (this.airline == null && product.getAirline() != null) {
            this.airline = product.getAirline().getNameKor();
        }
        
        // 출국편 정보 추출 (day 0의 첫 번째 AIR Location)
        if (product.getTour() != null && product.getTour().getSchedules() != null) {
            for (renewal.common.entity.Schedule schedule : product.getTour().getSchedules()) {
                if (schedule != null && schedule.getDay() != null && schedule.getDay() == 0L 
                    && schedule.getLocations() != null) {
                    for (renewal.common.entity.Location location : schedule.getLocations()) {
                        if (location != null && location.getLocationType() == LocationType.AIR
                            && location.getSeatClass() != null
                            && location.getSeatClass().getAir() != null) {
                            renewal.common.entity.Air air = location.getSeatClass().getAir();
                            this.departFlightDateTime = air.getDepartDateTime();
                            this.departFlightArriveDateTime = air.getArriveDateTime(); // 출국편 도착 시간 추가
                            this.departFlightNumber = air.getFlightNumber();
                            if (air.getAirline() != null) {
                                this.departFlightAirline = air.getAirline().getNameKor();
                            }
                            if (air.getDepartAirport() != null) {
                                this.departFlightDepartAirport = air.getDepartAirport().getAirportKor() != null 
                                    ? air.getDepartAirport().getAirportKor() 
                                    : air.getDepartAirport().getAirportEng();
                            }
                            if (air.getArriveAirport() != null) {
                                this.departFlightArriveAirport = air.getArriveAirport().getAirportKor() != null 
                                    ? air.getArriveAirport().getAirportKor() 
                                    : air.getArriveAirport().getAirportEng();
                            }
                            break;
                        }
                    }
                    if (this.departFlightDateTime != null) {
                        break;
                    }
                }
            }
            
            // 귀국편 정보 추출 (마지막 day의 첫 번째 AIR Location)
            Long lastDay = product.getTour().getSchedules().stream()
                .filter(Objects::nonNull)
                .mapToLong(s -> s.getDay() != null ? s.getDay() : 0L)
                .max()
                .orElse(0L);
            
            for (renewal.common.entity.Schedule schedule : product.getTour().getSchedules()) {
                if (schedule != null && schedule.getDay() != null && schedule.getDay().equals(lastDay)
                    && schedule.getLocations() != null) {
                    for (renewal.common.entity.Location location : schedule.getLocations()) {
                        if (location != null && location.getLocationType() == LocationType.AIR
                            && location.getSeatClass() != null
                            && location.getSeatClass().getAir() != null) {
                            renewal.common.entity.Air air = location.getSeatClass().getAir();
                            this.returnFlightDateTime = air.getDepartDateTime();
                            this.returnFlightArriveDateTime = air.getArriveDateTime(); // 귀국편 도착 시간 추가
                            this.returnFlightNumber = air.getFlightNumber();
                            if (air.getAirline() != null) {
                                this.returnFlightAirline = air.getAirline().getNameKor();
                            }
                            if (air.getDepartAirport() != null) {
                                this.returnFlightDepartAirport = air.getDepartAirport().getAirportKor() != null 
                                    ? air.getDepartAirport().getAirportKor() 
                                    : air.getDepartAirport().getAirportEng();
                            }
                            if (air.getArriveAirport() != null) {
                                this.returnFlightArriveAirport = air.getArriveAirport().getAirportKor() != null 
                                    ? air.getArriveAirport().getAirportKor() 
                                    : air.getArriveAirport().getAirportEng();
                            }
                            break;
                        }
                    }
                    if (this.returnFlightDateTime != null) {
                        break;
                    }
                }
            }
        }
        
        this.noShopping = product.isNoShopping();
        this.noOption = product.isNoOption();

        this.included = product.getInclude();
        this.excluded = product.getExclude();

        this.schedules = product.getTour().getSchedules();

        this.star1 = product.getStar1();
        this.star2 = product.getStar2();
        this.star3 = product.getStar3();
        this.star4 = product.getStar4();
        this.star5 = product.getStar5();

        this.productCode = product.getId() != null ? product.getId().toString() : "";

        // 가격 구성 요소 계산 (디버깅용)
        // 🔧 calcSingleProduct에서 이미 계산된 finalPriceAdult를 사용
        this.tourPrice = product.getTour() != null ? product.getTour().getPriceAdult() : 0L;
        this.totalAirPrice = 0L;
        this.totalHotelPrice = 0L;
        StringBuilder breakdown = new StringBuilder();
        
        if (product.getTour() != null && product.getTour().getSchedules() != null) {
            breakdown.append("투어 기본가: ").append(this.tourPrice != null ? String.format("%,d", this.tourPrice) : "0").append("원");
            
            // 🔧 Schedule을 day 순서대로 정렬하여 순차적으로 처리
            List<Schedule> sortedSchedules = product.getTour().getSchedules().stream()
                .filter(Objects::nonNull)
                .sorted((a, b) -> {
                    Long dayA = a.getDay() != null ? a.getDay() : 0L;
                    Long dayB = b.getDay() != null ? b.getDay() : 0L;
                    return dayA.compareTo(dayB);
                })
                .collect(java.util.stream.Collectors.toList());
            
            for (Schedule schedule : sortedSchedules) {
                if (schedule != null && schedule.getLocations() != null) {
                    // 🔧 Location을 locations_order 순서대로 정렬
                    List<Location> sortedLocations = schedule.getLocations().stream()
                        .filter(Objects::nonNull)
                        .sorted((a, b) -> {
                            // locations_order가 없으면 타입별로 정렬 (AIR -> HOTEL -> POINT)
                            if (a.getLocationType() == LocationType.AIR && b.getLocationType() != LocationType.AIR) {
                                return -1;
                            } else if (a.getLocationType() != LocationType.AIR && b.getLocationType() == LocationType.AIR) {
                                return 1;
                            }
                            return 0;
                        })
                        .collect(java.util.stream.Collectors.toList());
                    
                    for (Location location : sortedLocations) {
                        if (location != null) {
                            if (location.getLocationType() == LocationType.AIR) {
                                // 🔧 calcSingleProduct에서 설정된 SeatClass를 사용
                                // location.getSeatClass()가 null이면 calcSingleProduct에서 계산되지 않은 항공편이므로 건너뜀
                                if (location.getSeatClass() != null) {
                                    Long airPrice = location.getSeatClass().getPriceAdult();
                                    if (airPrice != null) {
                                        this.totalAirPrice += airPrice;
                                        String seatClassTypeStr = location.getSeatClass().getClassType() != null 
                                            ? location.getSeatClass().getClassType().name() 
                                            : "";
                                        breakdown.append(" + 항공(")
                                            .append(location.getDepartAirport() != null ? location.getDepartAirport().getAirportCode() : "")
                                            .append("→")
                                            .append(location.getArriveAirport() != null ? location.getArriveAirport().getAirportCode() : "")
                                            .append(", ").append(seatClassTypeStr).append("): ")
                                            .append(String.format("%,d", airPrice))
                                            .append("원");
                                    }
                                }
                                // 🔧 SeatClass가 null인 경우는 calcSingleProduct에서 건너뛴 항공편이므로 breakdown에도 포함하지 않음
                            } else if (location.getLocationType() == LocationType.HOTEL && location.getHotel() != null) {
                                Long hotelPrice = location.getHotel().getPrice();
                                if (hotelPrice != null) {
                                    this.totalHotelPrice += hotelPrice;
                                    breakdown.append(" + 호텔: ").append(String.format("%,d", hotelPrice)).append("원");
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 🔧 총액은 calcSingleProduct에서 계산된 finalPriceAdult를 사용 (이미 투어+항공+호텔 합산됨)
        // 🔧 breakdown은 참고용으로만 사용 (디버깅용)
        // 실제 가격은 finalPriceAdult를 기준으로 함
        breakdown.append(" = 총액: ").append(this.priceAdult != null ? String.format("%,d", this.priceAdult) : "0").append("원");
        
        this.priceBreakdown = breakdown.toString();

        // bak (HOTEL 숙박 횟수 계산)
        if (product.getTour() != null && product.getTour().getSchedules() != null) {
            this.bak = product.getTour().getSchedules().stream()
                    .filter(Objects::nonNull)
                    .flatMap(s -> s.getLocations().stream())
                    .filter(loc -> loc != null && loc.getLocationType() == LocationType.HOTEL)
                    .count();
        } else {
            this.bak = 0L;
        }

        // il 일수
        this.il = (long) product.getTour().getSchedules().size();

        this.reviews = null;

        // 타임딜 해당 상품인경우 할인가격
        if (product.getTimeDeal() != null && product.getTimeDeal().isActive()) {
            this.originalPriceAdult = product.getTimeDeal().getOriginalPriceAdult();
            this.originalPriceYouth = product.getTimeDeal().getOriginalPriceYouth();
            this.originalPriceInfant = product.getTimeDeal().getOriginalPriceInfant();
            this.discountType = product.getTimeDeal().getDiscountType();
            this.discountValue = product.getTimeDeal().getValue();
            this.startTime = product.getTimeDeal().getStartTime();
            this.endTime = product.getTimeDeal().getEndTime();
        }
    }

}
