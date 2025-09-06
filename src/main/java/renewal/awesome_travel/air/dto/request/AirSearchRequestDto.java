package renewal.awesome_travel.air.dto.request;

import lombok.Getter;
import lombok.Setter;
import renewal.common.entity.SeatClass.SeatClassType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AirSearchRequestDto {

    // 여행 타입: ROUND_TRIP / ONE_WAY / MULTI
    private String tripType;

    // 왕복/다구간 조합상품 여부
    private Boolean useCombinationSearch;

    // 편도/왕복 기본 경로
    private String depart;
    private String arrive;

    // 왕복: 가는날/오는날, 편도: 가는날
    private LocalDate departDateFrom;
    private LocalDate departDateTo;

    // 시간 필터 (출발 시간 범위)
    private LocalDateTime departTimeStart;
    private LocalDateTime departTimeEnd;

    // 가격 필터
    private Integer minPrice;
    private Integer maxPrice;

    // 항공사 필터
    private List<String> airlines;

    // 좌석 등급 (이코노미, 비즈니스 등)
    private SeatClassType seatClassType;

    // 인원수
    private int adultCount;
    private int childCount;
    private int infantCount;
    private boolean includeInfantInSeatCount;

    // 직항 여부
    private Boolean directOnly;

    // 정렬 기준 (ex: "price", "departTime", "stopovers")
    private String sortField;
    private String sortOrder; // "asc" / "desc"

    // 다구간일 경우 구간 리스트
    private List<SegmentRequest> multiSegments;

    // 페이징
    private int page;
    private int size;
}



