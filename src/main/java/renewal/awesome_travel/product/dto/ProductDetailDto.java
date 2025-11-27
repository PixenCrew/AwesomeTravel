package renewal.awesome_travel.product.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.common.entity.Location.LocationType;
import renewal.common.entity.Product;
import renewal.common.entity.Product.Info;
import renewal.common.entity.Product.ProductStatus;
import renewal.common.entity.Review;
import renewal.common.entity.Schedule;

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

    // 중간 정보 섹션
    private ProductStatus productStatus;
    private Long bak; // N박 숙박횟수
    private Long il; // M일
    private String airline; // 항공사 ( 출발항공사 이름 넣음 )
    private boolean noShopping; // 노쇼핑?
    private boolean noOption; // 노옵션?

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

        // this.productCode = product.getId() + "";
        this.productCode = "ASS879";

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

    }

}
