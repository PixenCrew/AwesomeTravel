package renewal.awesome_travel.product.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.common.entity.Product;
import renewal.common.entity.Product.DepartTimeType;
import renewal.common.entity.Product.ProductStatus;
import renewal.common.entity.TimeDeal.DiscountType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductCalanderDto {

    private Long id;

    private String title;

    private DepartTimeType departDateTimeType;
    private LocalDateTime departDateTime;
    private LocalDateTime returnDateTime;

    private Long price;
    private Long finalPriceAdult;
    private Long finalPriceYouth;
    private Long finalPriceInfant;
    private Long remainSeats;
    private ProductStatus status;
    private String seatClassType; // 좌석 등급 (ECONOMY, BUSINESS, FIRST 등)
    private String airline; // 항공사명

    // 타임딜 정보
    private Long originalPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private DiscountType discountType;
    private Long discountValue;

    public ProductCalanderDto(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.departDateTimeType = product.getDepartTimeType();
        this.departDateTime = product.getDepartDateTime();
        this.returnDateTime = product.getReturnDateTime();
        this.finalPriceAdult = product.getFinalPriceAdult();
        this.finalPriceYouth = product.getFinalPriceYouth();
        this.finalPriceInfant = product.getFinalPriceInfant();
        this.price = product.getFinalPriceAdult();
        this.remainSeats = product.getAvailableSeats();
        this.status = product.getProductStatus();
        
        // 항공사 정보 가져오기
        this.airline = null;
        if (product.getAirline() != null && product.getAirline().getNameKor() != null) {
            this.airline = product.getAirline().getNameKor();
        } else if (product.getTour() != null && product.getTour().getSchedules() != null) {
            // Product에 항공사 정보가 없으면 첫 번째 AIR Location의 seatClass에서 찾기
            for (renewal.common.entity.Schedule schedule : product.getTour().getSchedules()) {
                if (schedule != null && schedule.getLocations() != null) {
                    for (renewal.common.entity.Location location : schedule.getLocations()) {
                        if (location != null && location.getLocationType() == renewal.common.entity.Location.LocationType.AIR
                            && location.getSeatClass() != null
                            && location.getSeatClass().getAir() != null
                            && location.getSeatClass().getAir().getAirline() != null) {
                            this.airline = location.getSeatClass().getAir().getAirline().getNameKor();
                            if (this.airline != null) {
                                break;
                            }
                        }
                    }
                    if (this.airline != null) {
                        break;
                    }
                }
            }
        }
        
        // 모든 AIR Location의 좌석 등급 확인 (같은 등급끼리만 조합되므로 모두 같은 등급이어야 함)
        this.seatClassType = null;
        renewal.common.entity.SeatClass.SeatClassType firstClassType = null;
        boolean allSameClass = true;
        
        if (product.getTour() != null && product.getTour().getSchedules() != null) {
            for (renewal.common.entity.Schedule schedule : product.getTour().getSchedules()) {
                if (schedule != null && schedule.getLocations() != null) {
                    for (renewal.common.entity.Location location : schedule.getLocations()) {
                        if (location != null && location.getLocationType() == renewal.common.entity.Location.LocationType.AIR
                            && location.getSeatClass() != null) {
                            renewal.common.entity.SeatClass.SeatClassType classType = location.getSeatClass().getClassType();
                            if (classType != null) {
                                if (firstClassType == null) {
                                    firstClassType = classType;
                                } else if (firstClassType != classType) {
                                    // 다른 등급이 섞여있으면 null로 설정 (이론적으로는 발생하지 않아야 함)
                                    allSameClass = false;
                                    break;
                                }
                            }
                        }
                    }
                    if (!allSameClass) {
                        break;
                    }
                }
            }
            
            // 모든 항공편이 같은 등급이면 한국어로 변환하여 표시
            if (allSameClass && firstClassType != null) {
                switch (firstClassType) {
                    case ECONOMY:
                        this.seatClassType = "이코노미";
                        break;
                    case PREMIUMECONOMY:
                        this.seatClassType = "프리미엄 이코노미";
                        break;
                    case BUSINESS:
                        this.seatClassType = "비즈니스";
                        break;
                    case FIRST:
                        this.seatClassType = "퍼스트";
                        break;
                    default:
                        this.seatClassType = firstClassType.name();
                }
            }
        }

        // 타임딜 해당 상품인경우 할인가격
        if (product.getTimeDeal() != null && product.getTimeDeal().isActive()) {
            this.originalPrice = product.getTimeDeal().getOriginalPriceAdult();
            this.discountType = product.getTimeDeal().getDiscountType();
            this.discountValue = product.getTimeDeal().getValue();
            this.startTime = product.getTimeDeal().getStartTime();
            this.endTime = product.getTimeDeal().getEndTime();
        }
    }

}
