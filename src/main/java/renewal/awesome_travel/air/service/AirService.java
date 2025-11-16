package renewal.awesome_travel.air.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.air.dto.AirDetailRequestDto;
import renewal.awesome_travel.air.dto.AirDetailResponseDto;
import renewal.awesome_travel.air.dto.AirSearchRequestDto;
import renewal.awesome_travel.air.dto.AirSearchRequestDto.RequestSegment;
import renewal.awesome_travel.air.dto.AirSearchResponseDto;
import renewal.awesome_travel.air.dto.AirSearchResponseDto.Trip;
import renewal.awesome_travel.air.repository.AirRepository;
import renewal.common.entity.Air;
import renewal.common.entity.AirportCode;
import renewal.common.entity.SeatClass;
import renewal.common.entity.SeatClass.SeatClassType;

@Service
@RequiredArgsConstructor
public class AirService {

    private final AirRepository airRepository;

    public List<AirSearchResponseDto> searchAir(AirSearchRequestDto request) {

        // 첫번째 구간 검색 -> 첫 List에 저장
        // 두번째 구간 검색 -> 두번째 List에 저장
        // ...
        // 구간들 카르티시안 조합해 AirSearchResponseDto 생성

        // 공통 값
        Long seatCount = request.getAdultCount() + request.getYouthCount();
        Long seatCountAdult = request.getAdultCount();
        Long seatCountYouth = request.getYouthCount();
        Long seatCountInfant = request.getInfantCount();
        SeatClassType seatClassType = request.getSeatClassType();
        boolean directOnly = request.isDirectOnly();

        // 구간별 변수
        AirportCode depart;
        AirportCode arrive;
        LocalDate departDate;

        // 구간들 리스트
        List<RequestSegment> segments = request.getSegments();

        // 검색결과 리스트
        List<List<SeatClass>> queryResult = new ArrayList<List<SeatClass>>();

        for (RequestSegment requestSegment : segments) {

            // 각 구간의 출발-도착-출발일
            depart = requestSegment.getDepart();
            arrive = requestSegment.getArrive();
            departDate = requestSegment.getDepartDate();

            List<SeatClass> current = airRepository.searchSegment(seatCount, seatClassType, directOnly, depart,
                    arrive, departDate);

            // System.out.println("========current" + current);

            queryResult.add(current);
        }

        // 결과 리스트 초기화
        List<AirSearchResponseDto> result = new ArrayList<AirSearchResponseDto>();
        List<List<SeatClass>> cartesianList = cartesianProduct(queryResult);

        for (List<SeatClass> unitlist : cartesianList) {
            AirSearchResponseDto current = new AirSearchResponseDto();
            Long priceSum = 0L;
            List<Trip> currentTrips = current.getTripList();
            for (SeatClass seat : unitlist) {
                Trip currentTrip = new Trip();
                Air currentAir = seat.getAir();
                priceSum += seat.getPriceAdult() * seatCountAdult + seat.getPriceYouth() * seatCountYouth
                        + seat.getPriceInfant() * seatCountInfant;

                // 항공사
                currentTrip.setAirline(currentAir.getAirline());
                currentTrip.setFlightNumber(currentAir.getFlightNumber());

                // 출발
                currentTrip.setDepart(currentAir.getDepartAirport());
                currentTrip.setDepartDateTime(currentAir.getDepartDateTime());

                // 비행중
                currentTrip.setFlightDurationTotal(currentAir.getFlightDuration());
                currentTrip.setStopOvers(currentAir.getStopovers());

                // 도착
                currentTrip.setArrive(currentAir.getArriveAirport());
                currentTrip.setArriveDateTime(currentAir.getArriveDateTime());

                // 항공편 경로 상세
                currentTrip.setFlightSegments(currentAir.getFlightSegments());

                // seatClass id 입력
                currentTrip.setSeatClassId(seat.getId());

                currentTrips.add(currentTrip);
            }
            current.setPrice(priceSum);
            result.add(current);
        }
        return result;
    }

    // 재사용 가능한 카르티시안 곱 함수
    private List<List<SeatClass>> cartesianProduct(List<List<SeatClass>> lists) {
        List<List<SeatClass>> resultLists = new ArrayList<>();
        if (lists.isEmpty()) {
            resultLists.add(new ArrayList<>());
            return resultLists;
        } else {
            List<SeatClass> firstList = lists.get(0);
            List<List<SeatClass>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
            for (SeatClass condition : firstList) {
                for (List<SeatClass> remaining : remainingLists) {
                    List<SeatClass> resultList = new ArrayList<>();
                    resultList.add(condition);
                    resultList.addAll(remaining);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }

    public AirDetailResponseDto calculateAirDetail(AirDetailRequestDto detailRequest, List<SeatClass> seatClasses) {

        long PRICEADULT = 0L;
        long PRICEYOUTH = 0L;
        long PRICEINFANT = 0L;

        for (SeatClass seat : seatClasses) {
            PRICEADULT += seat.getPriceAdult();
            PRICEYOUTH += seat.getPriceYouth();
            PRICEINFANT += seat.getPriceInfant();
        }

        // 정률 계산
        final double OIL_RATE = 0.25;
        final double TAX_RATE = 0.12;
        final long FEE = 5000L;

        int a = detailRequest.getAdultCount();
        int y = detailRequest.getYouthCount();
        int i = detailRequest.getInfantCount();

        // ------- 성인 -------
        long perAdultOil = Math.round(PRICEADULT * OIL_RATE);
        long perAdultTax = Math.round(PRICEADULT * TAX_RATE);
        long perAdultFee = FEE;
        long perAdultBase = PRICEADULT - perAdultOil - perAdultTax - perAdultFee;

        long adultOil = perAdultOil * a;
        long adultTax = perAdultTax * a;
        long adultFee = perAdultFee * a;
        long adultBase = perAdultBase * a;
        long adultTotal = PRICEADULT * a;

        // ------- 청소년 -------
        long perYouthOil = Math.round(PRICEYOUTH * OIL_RATE);
        long perYouthTax = Math.round(PRICEYOUTH * TAX_RATE);
        long perYouthFee = FEE;
        long perYouthBase = PRICEYOUTH - perYouthOil - perYouthTax - perYouthFee;

        long youthOil = perYouthOil * y;
        long youthTax = perYouthTax * y;
        long youthFee = perYouthFee * y;
        long youthBase = perYouthBase * y;
        long youthTotal = PRICEYOUTH * y;

        // ------- 유아 -------
        long perInfantOil = Math.round(PRICEINFANT * OIL_RATE);
        long perInfantTax = Math.round(PRICEINFANT * TAX_RATE);
        long perInfantFee = FEE;
        long perInfantBase = PRICEINFANT - perInfantOil - perInfantTax - perInfantFee;

        long infantOil = perInfantOil * i;
        long infantTax = perInfantTax * i;
        long infantFee = perInfantFee * i;
        long infantBase = perInfantBase * i;
        long infantTotal = PRICEINFANT * i;

        long priceTotal = adultTotal + youthTotal + infantTotal;

        // DTO 리턴
        return AirDetailResponseDto.builder()
                .detailRequest(detailRequest)
                .seatClasses(seatClasses)

                .priceAdult(PRICEADULT)
                .priceYouth(PRICEYOUTH)
                .priceInfant(PRICEINFANT)

                .adultBase(adultBase)
                .adultOil(adultOil)
                .adultTax(adultTax)
                .adultFee(adultFee)
                .adultTotal(adultTotal)

                .youthBase(youthBase)
                .youthOil(youthOil)
                .youthTax(youthTax)
                .youthFee(youthFee)
                .youthTotal(youthTotal)

                .infantBase(infantBase)
                .infantOil(infantOil)
                .infantTax(infantTax)
                .infantFee(infantFee)
                .infantTotal(infantTotal)

                .priceTotal(priceTotal)
                .build();
    }

}