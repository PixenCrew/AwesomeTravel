package renewal.awesome_travel.air.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import renewal.awesome_travel.air.dto.response.SeatClassDetailResponseDto;
import renewal.common.entity.SeatClass;
import renewal.awesome_travel.air.repository.SeatClassRepository;

@Service
@RequiredArgsConstructor
public class SeatClassService {

    // private final SeatClassRepository seatClassRepository;

    // @Transactional(readOnly = true)
    // public SeatClassDetailResponseDto getSeatClassDetail(Long seatClassId) {
    //     SeatClass seat = seatClassRepository.findById(seatClassId)
    //             .orElseThrow(() -> new IllegalArgumentException("좌석 클래스 정보가 존재하지 않습니다."));

    //     return SeatClassDetailResponseDto.builder()
    //             .seatClassId(seat.getId())
    //             .airCode(seat.getAir().getCode())
    //             .airlineName(seat.getAir().getAirline().getCode())
    //             .depart(seat.getAir().getDepart())
    //             .arrive(seat.getAir().getArrive())
    //             .departTime(seat.getAir().getDepart_time())
    //             .arriveTime(seat.getAir().getArrive_time())
    //             .classType(seat.getClassType())
    //             .price(seat.getPrice())
    //             .availableSeats(seat.getAvailableSeats())
    //             .build();
    // }
}
