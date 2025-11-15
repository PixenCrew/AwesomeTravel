package renewal.awesome_travel.air.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import renewal.common.entity.SeatClass;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirDetailResponseDto {

    private AirDetailRequestDto detailRequest; // 요청 정보 그대로 반환

    private List<SeatClass> seatClasses; // 선택된 좌석 클래스 목록

    // 단가 합계
    private long priceAdult;
    private long priceYouth;
    private long priceInfant;

    // 성인 가격 구성
    private long adultBase;
    private long adultOil;
    private long adultTax;
    private long adultFee;
    private long adultTotal;

    // 청소년 가격 구성
    private long youthBase;
    private long youthOil;
    private long youthTax;
    private long youthFee;
    private long youthTotal;

    // 영유아 가격 구성
    private long infantBase;
    private long infantOil;
    private long infantTax;
    private long infantFee;
    private long infantTotal;

    // 최종합계
    private long priceTotal;
}
