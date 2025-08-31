package renewal.awesome_travel.purchase.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import renewal.awesome_travel.air.dto.response.AirResponseDto;
import renewal.awesome_travel.air.entity.Air;
import renewal.awesome_travel.air.entity.SeatClass;
import renewal.awesome_travel.air.repository.AirRepository;
import renewal.awesome_travel.air.repository.SeatClassRepository;
import renewal.awesome_travel.purchase.dto.requestDto.AirPassengerRequestDto;
import renewal.awesome_travel.purchase.dto.requestDto.AirPassengerUpdateRequestDto;
import renewal.awesome_travel.purchase.dto.requestDto.AirPurchaseRequestDto;
import renewal.awesome_travel.purchase.dto.responseDto.AirPassengerResponseDto;
import renewal.awesome_travel.purchase.dto.responseDto.AirPurchaseResponseDto;
import renewal.awesome_travel.purchase.entity.Country;
import renewal.awesome_travel.purchase.repository.AirPurchaseRepository;
import renewal.awesome_travel.purchase.repository.CountryRepository;
import renewal.awesome_travel.purchase.repository.SpecialRequestRepository;
import renewal.awesome_travel.purchase.utiles.PurchaseStatus;
import renewal.common.entity.AirPassenger;
import renewal.common.entity.AirPurchase;
import renewal.common.entity.SpecialRequest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AirPurchaseService {

    private final AirPurchaseRepository airPurchaseRepository;
    private final SeatClassRepository seatClassRepository;
    private final CountryRepository countryRepository;
    private final SpecialRequestRepository specialRequestRepository;

    // 1. 예약 (좌석 임시 점유 + 상태 HOLDING)
    @Transactional
    public Long holdSeats(AirPurchaseRequestDto request) {
        int adultCount = request.getAdultCount();
        int childCount = request.getChildCount();
        int infantCount = request.getInfantCount();

        if (infantCount > adultCount) {
            throw new IllegalArgumentException("유아 수는 성인 수를 초과할 수 없습니다.");
        }

        int reserveCount = adultCount + childCount;

        SeatClass seatClass = seatClassRepository.findByIdWithLock(request.getSeatClassId())
                .orElseThrow(() -> new IllegalArgumentException("좌석 클래스 없음"));

        seatClass.decreaseAvailableSeats(reserveCount);

        AirPurchase airPurchase = new AirPurchase(
                seatClass,
                seatClass.getPrice() * reserveCount,
                request.getMemberId(),
                request.getName(),
                request.getNumber(),
                request.getEmail(),
                request.getPurchaseDate(),
                request.getPaymentDueDate()
        );
        airPurchase.setPurchaseStatus(PurchaseStatus.HOLDING);

        for (AirPassengerRequestDto p : request.getPassengers()) {
            Country country = countryRepository.findById(p.getCountryCode())
                    .orElseThrow(() -> new IllegalArgumentException("국가 없음"));

            AirPassenger passenger = new AirPassenger(
                    airPurchase,
                    p.getName(), p.getNumber(), p.getEmail(), p.getBirth(),
                    p.getSex(), country, p.getPassportNum(), p.getLastName(),
                    p.getFirstName(), p.getExpire()
            );

            Set<SpecialRequest> requests = new HashSet<>(specialRequestRepository.findAllById(p.getSpecialRequestIds()));
            passenger.addSpecialRequests(requests);
            airPurchase.getAirPassengers().add(passenger);
        }

        return airPurchaseRepository.save(airPurchase).getId();
    }

    // 2. 결제 확정 처리
    @Transactional
    public void confirmPayment(Long purchaseId) {
        AirPurchase purchase = airPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("구매 내역 없음"));

        if (purchase.getPurchaseStatus() != PurchaseStatus.HOLDING) {
            throw new IllegalStateException("이미 처리된 구매입니다.");
        }

        purchase.setPurchaseStatus(PurchaseStatus.PAID); // 확정
    }

    // 3. 자동 취소 (스케줄러용)
    @Transactional
    public void cancelExpiredHolds() {
        int page = 0;
        int size = 100;

        while (true) {
            Page<AirPurchase> expiredPage = airPurchaseRepository
                    .findByPurchaseStatusAndPaymentDueDateBefore(
                            PurchaseStatus.HOLDING,
                            LocalDateTime.now(),
                            PageRequest.of(page, size)
                    );

            if (expiredPage.isEmpty()) break;

            for (AirPurchase p : expiredPage) {
                p.getSeatClass().increaseAvailableSeats(p.getAirPassengers().size());
                p.setPurchaseStatus(PurchaseStatus.CANCELLED);
            }

            page++;
        }
    }



    // 4. 상세 조회
    public AirPurchaseResponseDto getPurchase(Long id) {
        AirPurchase purchase = airPurchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("구매 내역 없음"));

        SeatClass seatClass = purchase.getSeatClass();
        Air air = seatClass.getAir();

        AirResponseDto airDto = AirResponseDto.builder()
                .airId(air.getId())
                .code(air.getCode())
                .airlineCode(air.getAirline().getCode())
                .airlineNameKor(air.getAirline().getNameKor())
                .airlineNameEng(air.getAirline().getNameEng())
                .depart(air.getDepart())
                .arrive(air.getArrive())
                .departTime(air.getDepart_time())
                .arriveTime(air.getArrive_time())
                .stopovers(air.getStopovers())
                .flightType(air.getFlightType())
                .seatClassId(seatClass.getId())
                .seatClassType(seatClass.getClassType())
                .price(seatClass.getPrice())
                .availableSeats(seatClass.getAvailableSeats())
                .build();

        List<AirPassengerResponseDto> passengerDtos = purchase.getAirPassengers().stream()
                .map(passenger -> {
                    List<String> requestList = passenger.getSpecialRequests().stream()
                            .map(SpecialRequest::getRequestType)
                            .toList();
                    return new AirPassengerResponseDto(
                            passenger.getName(),
                            passenger.getNumber(),
                            passenger.getEmail(),
                            passenger.getBirth(),
                            passenger.getSex().name(),
                            passenger.getNationality().getCountryCode(),
                            passenger.getPassport_num(),
                            passenger.getLastName(),
                            passenger.getFirstName(),
                            passenger.getExpire(),
                            requestList
                    );
                }).toList();

        return new AirPurchaseResponseDto(
                purchase.getId(),
                airDto,
                purchase.getPurchaseStatus(),
                purchase.getPrice(),
                purchase.getMember_id(),
                purchase.getName(),
                purchase.getNumber(),
                purchase.getEmail(),
                purchase.getPurchaseDate(),
                purchase.getPaymentDueDate(),
                passengerDtos
        );
    }


    // 5. 예약 취소
    @Transactional
    public void cancelPurchase(Long id) {
        AirPurchase purchase = airPurchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않음"));

        if (purchase.getPurchaseStatus() == PurchaseStatus.PAID) {
            throw new IllegalStateException("이미 결제된 예약은 취소할 수 없습니다.");
        }

        SeatClass seatClass = purchase.getSeatClass();
        seatClass.increaseAvailableSeats(purchase.getAirPassengers().size());

        purchase.setPurchaseStatus(PurchaseStatus.CANCELLED);
    }

    // 6. 탑승객 정보 수정
    @Transactional
    public void updatePassenger(Long purchaseId, Long passengerId, AirPassengerUpdateRequestDto dto) {
        AirPurchase purchase = airPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("구매 내역 없음"));

        if (purchase.getPurchaseStatus() != PurchaseStatus.HOLDING) {
            throw new IllegalStateException("결제 완료된 예약은 수정할 수 없습니다.");
        }

        AirPassenger passenger = purchase.getAirPassengers().stream()
                .filter(p -> p.getId().equals(passengerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("탑승자 없음"));

        Country country = null;
        if (dto.getCountryCode() != null) {
            country = countryRepository.findById(dto.getCountryCode())
                    .orElseThrow(() -> new IllegalArgumentException("국가 없음"));
        }

        Set<SpecialRequest> requests = null;
        if (dto.getSpecialRequestIds() != null) {
            requests = new HashSet<>(specialRequestRepository.findAllById(dto.getSpecialRequestIds()));
        }

        passenger.updateInfo(dto, country, requests);
    }


}

