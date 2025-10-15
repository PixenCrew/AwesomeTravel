package renewal.awesome_travel.purchase.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import renewal.awesome_travel.air.dto.response.AirResponseDto;
import renewal.awesome_travel.air.repository.SeatClassRepository;
import renewal.awesome_travel.purchase.dto.requestDto.PassengerAirRequestDto;
import renewal.awesome_travel.purchase.dto.requestDto.PassengerAirUpdateRequestDto;
import renewal.awesome_travel.purchase.dto.requestDto.PurchaseAirRequestDto;
import renewal.awesome_travel.purchase.dto.responseDto.PassengerAirResponseDto;
import renewal.awesome_travel.purchase.dto.responseDto.PurchaseAirResponseDto;
import renewal.awesome_travel.purchase.repository.PurchaseAirRepository;
import renewal.awesome_travel.purchase.repository.SpecialRequestRepository;
import renewal.common.entity.Air;
import renewal.common.entity.SeatClass;
import renewal.common.entity.CountryCode;
import renewal.common.entity.PurchaseBase.PurchaseStatus;
import renewal.common.entity.PassengerAir;
import renewal.common.entity.PurchaseAir;
import renewal.common.entity.SpecialRequest;
import renewal.common.repository.CountryCodeRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PurchaseAirService {

    private final PurchaseAirRepository airPurchaseRepository;
    private final SeatClassRepository seatClassRepository;
    private final CountryCodeRepository countryRepository;
    private final SpecialRequestRepository specialRequestRepository;

    // // 1. 예약 (좌석 임시 점유 + 상태 HOLDING)
    // @Transactional
    // public Long holdSeats(PurchaseAirRequestDto request) {
    //     int adultCount = request.getAdultCount();
    //     int childCount = request.getChildCount();
    //     int infantCount = request.getInfantCount();

    //     if (infantCount > adultCount) {
    //         throw new IllegalArgumentException("유아 수는 성인 수를 초과할 수 없습니다.");
    //     }

    //     int reserveCount = adultCount + childCount;

    //     SeatClass seatClass = seatClassRepository.findByIdWithLock(request.getSeatClassId())
    //             .orElseThrow(() -> new IllegalArgumentException("좌석 클래스 없음"));

    //     seatClass.decreaseAvailableSeats(reserveCount);

    //     PurchaseAir airPurchase = new PurchaseAir(
    //             seatClass,
    //             seatClass.getPrice() * reserveCount,
    //             request.getUser(),
    //             request.getName(),
    //             request.getNumber(),
    //             request.getEmail(),
    //             request.getPurchaseDate(),
    //             request.getPaymentDueDate());
    //     airPurchase.setPurchaseStatus(PurchaseStatus.HOLDING);

    //     for (PassengerAirRequestDto p : request.getPassengers()) {
    //         CountryCode country = countryRepository.findById(p.getCountryCode())
    //                 .orElseThrow(() -> new IllegalArgumentException("국가 없음"));

    //         PassengerAir passenger = new PassengerAir(
    //                 airPurchase,
    //                 p.getName(), p.getNumber(), p.getEmail(), p.getBirth(),
    //                 p.getSex(), country, p.getPassportNum(), p.getLastName(),
    //                 p.getFirstName(), p.getExpire());

    //         Set<SpecialRequest> requests = new HashSet<>(
    //                 specialRequestRepository.findAllById(p.getSpecialRequestIds()));
    //         passenger.addSpecialRequests(requests);
    //         airPurchase.getAirPassengers().add(passenger);
    //     }

    //     return airPurchaseRepository.save(airPurchase).getId();
    // }

    // // 2. 결제 확정 처리
    // @Transactional
    // public void confirmPayment(Long purchaseId) {
    //     PurchaseAir purchase = airPurchaseRepository.findById(purchaseId)
    //             .orElseThrow(() -> new IllegalArgumentException("구매 내역 없음"));

    //     if (purchase.getPurchaseStatus() != PurchaseStatus.HOLDING) {
    //         throw new IllegalStateException("이미 처리된 구매입니다.");
    //     }

    //     purchase.setPurchaseStatus(PurchaseStatus.PAID); // 확정
    // }

    // // 3. 자동 취소 (스케줄러용)
    // @Transactional
    // public void cancelExpiredHolds() {
    //     int page = 0;
    //     int size = 100;

    //     while (true) {
    //         Page<PurchaseAir> expiredPage = airPurchaseRepository
    //                 .findByPurchaseStatusAndPaymentDueDateBefore(
    //                         PurchaseStatus.HOLDING,
    //                         LocalDateTime.now(),
    //                         PageRequest.of(page, size));

    //         if (expiredPage.isEmpty())
    //             break;

    //         for (PurchaseAir p : expiredPage) {
    //             p.getSeatClass().increaseAvailableSeats(p.getAirPassengers().size());
    //             p.setPurchaseStatus(PurchaseStatus.CANCELLED);
    //         }

    //         page++;
    //     }
    // }

    // // 4. 상세 조회
    // public PurchaseAirResponseDto getPurchase(Long id) {
    //     PurchaseAir purchase = airPurchaseRepository.findById(id)
    //             .orElseThrow(() -> new IllegalArgumentException("구매 내역 없음"));

    //     SeatClass seatClass = purchase.getSeatClass();
    //     Air air = seatClass.getAir();

    //     PassengerAirResponseDto airDto = PassengerAirResponseDto.builder()
    //             // .airId(air.getId())
    //             // .code(air.getCode())
    //             .airlineCode(air.getAirline().getCode())
    //             .airlineNameKor(air.getAirline().getNameKor())
    //             .airlineNameEng(air.getAirline().getNameEng())
    //             .depart(air.getDepartAirport())
    //             .arrive(air.getArriveAirport())
    //             .departTime(air.getDepartTime())
    //             .arriveTime(air.getArriveTime())
    //             .stopovers(air.getStopovers())
    //             .flightType(air.getFlightType())
    //             .seatClassId(seatClass.getId())
    //             .seatClassType(seatClass.getClassType())
    //             .price(seatClass.getPrice())
    //             .availableSeats(seatClass.getAvailableSeats())
    //             .build();

    //     List<PassengerAirResponseDto> passengerDtos = purchase.getPassengerAirs().stream()
    //             .map(passenger -> {
    //                 List<String> requestList = passenger.getSpecialRequests().stream()
    //                         .map(SpecialRequest::getRequestType)
    //                         .toList();
    //                 return new PassengerAirResponseDto(
    //                         passenger.getName(),
    //                         passenger.getNumber(),
    //                         passenger.getEmail(),
    //                         passenger.getBirth(),
    //                         passenger.getSex().name(),
    //                         passenger.getNationality(),
    //                         passenger.getPassportNum(),
    //                         passenger.getLastName(),
    //                         passenger.getFirstName(),
    //                         passenger.getExpire(),
    //                         requestList);
    //             }).toList();

    //     return new PurchaseAirResponseDto(
    //             purchase.getId(),
    //             airDto,
    //             purchase.getPurchaseStatus(),
    //             purchase.getPrice(),
    //             purchase.getUser(),
    //             purchase.getName(),
    //             purchase.getNumber(),
    //             purchase.getEmail(),
    //             purchase.getPurchaseDate(),
    //             purchase.getPaymentDueDate(),
    //             passengerDtos);
    // }

    // // 5. 예약 취소
    // @Transactional
    // public void cancelPurchase(Long id) {
    //     PurchaseAir purchase = airPurchaseRepository.findById(id)
    //             .orElseThrow(() -> new IllegalArgumentException("존재하지 않음"));

    //     if (purchase.getPurchaseStatus() == PurchaseStatus.PAID) {
    //         throw new IllegalStateException("이미 결제된 예약은 취소할 수 없습니다.");
    //     }

    //     SeatClass seatClass = purchase.getSeatClass();
    //     seatClass.increaseAvailableSeats(purchase.getPassengerAirs().size());

    //     purchase.setPurchaseStatus(PurchaseStatus.CANCELLED);
    // }

    // // 6. 탑승객 정보 수정
    // @Transactional
    // public void updatePassenger(Long purchaseId, Long passengerId, PassengerAirUpdateRequestDto dto) {
    //     PurchaseAir purchase = airPurchaseRepository.findById(purchaseId)
    //             .orElseThrow(() -> new IllegalArgumentException("구매 내역 없음"));

    //     if (purchase.getPurchaseStatus() != PurchaseStatus.HOLDING) {
    //         throw new IllegalStateException("결제 완료된 예약은 수정할 수 없습니다.");
    //     }

    //     PassengerAir passenger = purchase.getPassengerAirs().stream()
    //             .filter(p -> p.getId().equals(passengerId))
    //             .findFirst()
    //             .orElseThrow(() -> new IllegalArgumentException("탑승자 없음"));

    //     if (dto.getCountryCode() != null) {
    //         countryRepository.findById(dto.getCountryCode().getCode())
    //                 .orElseThrow(() -> new IllegalArgumentException("국가 없음"));
    //     }

    //     Set<SpecialRequest> requests = null;
    //     if (dto.getSpecialRequestIds() != null) {
    //         requests = new HashSet<>(specialRequestRepository.findAllById(dto.getSpecialRequestIds()));
    //     }

    //     // PassengerBase 필드 처리
    //     if (dto.getName() != null)
    //         passenger.updateName(dto.getName());
    //     if (dto.getNumber() != null)
    //         passenger.updateNumber(dto.getNumber());
    //     if (dto.getEmail() != null)
    //         passenger.updateEmail(dto.getEmail());
    //     if (dto.getBirth() != null)
    //         passenger.updateBirth(dto.getBirth());
    //     if (dto.getSex() != null)
    //         passenger.updateSex(dto.getSex());
    //     if (dto.getCountryCode() != null)
    //         passenger.updateNationality(dto.getCountryCode());
    //     if (dto.getPassportNum() != null)
    //         passenger.updatePassportNum(dto.getPassportNum());
    //     if (dto.getLastName() != null)
    //         passenger.updateLastName(dto.getLastName());
    //     if (dto.getFirstName() != null)
    //         passenger.updateFirstName(dto.getFirstName());
    //     if (dto.getExpire() != null)
    //         passenger.updateExpire(dto.getExpire());

    //     // PassengerAir 고유 필드 처리
    //     if (requests != null) {
    //         Set<SpecialRequest> specialRequests = passenger.getSpecialRequests();
    //         specialRequests.clear();
    //         specialRequests.addAll(requests);
    //     }
    // }

}
