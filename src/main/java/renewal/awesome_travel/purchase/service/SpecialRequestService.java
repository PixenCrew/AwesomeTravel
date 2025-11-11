package renewal.awesome_travel.purchase.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecialRequestService {

    // private final SpecialRequestRepository specialRequestRepository;

    // // 사용자용 - 전체 특별요청 목록 조회
    // public List<SpecialRequestDto> getAllSpecialRequests() {
    // return specialRequestRepository.findAll().stream()
    // .map(req -> new SpecialRequestDto(
    // req.getId(),
    // req.getRequestType(),
    // req.getDescription()
    // ))
    // .toList();
    // }
}
