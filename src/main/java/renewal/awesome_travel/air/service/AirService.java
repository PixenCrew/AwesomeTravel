package renewal.awesome_travel.air.service;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import renewal.awesome_travel.air.dto.AirSearchRequestDto;
import renewal.awesome_travel.air.dto.AirSearchRequestDto.RequestSegment;
import renewal.awesome_travel.air.dto.AirSearchResponseDto;
import renewal.awesome_travel.air.dto.AirSearchResponseDto.Trip;
import renewal.awesome_travel.air.repository.AirRepository;
import renewal.common.entity.Air;
import renewal.common.entity.CityCode;
import renewal.common.entity.SeatClass;
import renewal.common.entity.SeatClass.SeatClassType;

@Service
@RequiredArgsConstructor
public class AirService {

    private final AirRepository airRepository;

    public List<AirSearchResponseDto> searchAir(AirSearchRequestDto request) {

        // TODO
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
        Boolean directOnly = request.getDirectOnly();

        // 구간별 변수
        CityCode depart;
        CityCode arrive;
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
            
                    System.out.println("========current"+current);

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
                priceSum+=seat.getPriceAdult()*seatCountAdult + seat.getPriceYouth()*seatCountYouth + seat.getPriceInfant()*seatCountInfant;
                
                // 항공사
                currentTrip.setAirline(currentAir.getAirline());
                currentTrip.setFlightNumber(currentAir.getFlightNumber());

                // 출발
                currentTrip.setDepart(currentAir.getDepartAirport());
                currentTrip.setDepartDateTime(currentAir.getArriveDateTime());

                // 비행중
                currentTrip.setFlightDurationTotal(currentAir.getFlightDuration());
                currentTrip.setStopOvers(currentAir.getStopovers());

                // 도착
                currentTrip.setArrive(currentAir.getArriveAirport());
                currentTrip.setArriveDateTime(currentAir.getArriveDateTime());

                // 항공편 경로 상세
                currentTrip.setFlightSegments(currentAir.getFlightSegments());

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

    // private FlightSegment airToSegment(Air air){
    //     FlightSegment segment = new FlightSegment();

    //     return segment;
    // }

}