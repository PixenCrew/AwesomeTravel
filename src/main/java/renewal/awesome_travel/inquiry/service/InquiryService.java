package renewal.awesome_travel.inquiry.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.inquiry.dto.request.InquiryAnswerRequestDto;
import renewal.awesome_travel.inquiry.dto.request.InquiryRequestDto;
import renewal.awesome_travel.inquiry.dto.response.InquiryAnswerResponseDto;
import renewal.awesome_travel.inquiry.dto.response.InquiryDetailResponseDto;
import renewal.awesome_travel.inquiry.dto.response.InquiryResponseDto;
import renewal.awesome_travel.inquiry.repository.InquiryAnswerRepository;
import renewal.awesome_travel.inquiry.repository.InquiryRepository;
import renewal.awesome_travel.notification.repository.NotificationRepository;
import renewal.awesome_travel.review.util.ProfanityFilter;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.common.entity.Inquiry;
import renewal.common.entity.Inquiry.InquiryStage;
import renewal.common.entity.InquiryAnswer;
import renewal.common.entity.Notification;
import renewal.common.entity.PurchaseAir;
import renewal.common.entity.PurchaseProduct;
import renewal.common.entity.User;
import renewal.common.repository.ProductRepository;
import renewal.common.repository.PurchaseAirRepository;
import renewal.common.repository.PurchaseProductRepository;

@Service
@RequiredArgsConstructor
public class InquiryService {

        private final InquiryRepository inquiryRepository;
        private final InquiryAnswerRepository inquiryAnswerRepository;
        private final NotificationRepository notificationRepository;
        private final UserRepository userRepository;
        private final PurchaseProductRepository purchaseProductRepo;
        private final PurchaseAirRepository purchaseAirRepo;
        private final ProductRepository productRepo;
        private final ProfanityFilter profanityFilter;

        // 문의 작성
        public Long createInquiry(Long userId, InquiryRequestDto dto) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));
                
                // 비속어 필터링 (제목과 내용 모두 체크)
                if (profanityFilter.containsProfanity(dto.getTitle())) {
                        throw new IllegalArgumentException("부적절한 단어가 포함되어 있습니다. 문의를 작성할 수 없습니다.");
                }
                if (profanityFilter.containsProfanity(dto.getContent())) {
                        throw new IllegalArgumentException("부적절한 단어가 포함되어 있습니다. 문의를 작성할 수 없습니다.");
                }
                
                // stage 우선순위: DTO에서 명시적으로 설정된 값 > purchaseId 기반 자동 설정 > productId 기반 자동 설정 > 기본값
                InquiryStage stage = InquiryStage.GENERAL;
                
                if (dto.getStage() != null) {
                        // DTO에서 명시적으로 설정된 경우
                        stage = dto.getStage();
                } else if (dto.getPurchaseId() != null) {
                        // purchaseId가 있으면 purchase 상태에 따라 자동 설정
                        // PurchaseProduct 또는 PurchaseAir 중 하나를 찾아서 처리
                        PurchaseProduct purchaseProduct = purchaseProductRepo.findById(dto.getPurchaseId()).orElse(null);
                        PurchaseAir purchaseAir = purchaseAirRepo.findById(dto.getPurchaseId()).orElse(null);
                        
                        if (purchaseProduct != null) {
                                switch (purchaseProduct.getPurchaseStatus()) {
                                        case RESERVED:
                                        case CONFIRMED:
                                        case HOLDING:
                                                stage = InquiryStage.AFTER_BOOKING;
                                                break;
                                        case PAID:
                                                if (LocalDateTime.now().isBefore(purchaseProduct.getDepartDateTime())) {
                                                        stage = InquiryStage.AFTER_BOOKING;
                                                } else {
                                                        stage = InquiryStage.AFTER_TRAVEL;
                                                }
                                                break;
                                        case CANCELLED:
                                                stage = InquiryStage.GENERAL;
                                                break;
                                }
                        } else if (purchaseAir != null) {
                                switch (purchaseAir.getPurchaseStatus()) {
                                        case RESERVED:
                                        case CONFIRMED:
                                        case HOLDING:
                                                stage = InquiryStage.AFTER_BOOKING;
                                                break;
                                        case PAID:
                                                // PurchaseAir의 경우 departDateTime이 없을 수 있으므로 기본적으로 AFTER_BOOKING으로 설정
                                                stage = InquiryStage.AFTER_BOOKING;
                                                break;
                                        case CANCELLED:
                                                stage = InquiryStage.GENERAL;
                                                break;
                                }
                        } else {
                                throw new IllegalArgumentException("구매 내역을 찾을 수 없습니다.");
                        }
                } else if (dto.getProductId() != null && productRepo.findById(dto.getProductId()).isPresent()) {
                        // productId만 있는 경우 (구매 전 문의)
                        stage = InquiryStage.BEFORE_PURCHASE;
                }
                
                Inquiry inquiry = new Inquiry(
                                user,
                                dto.getTitle(),
                                dto.getContent(),
                                dto.getCategory(),
                                dto.getProductId(),
                                dto.getPurchaseId(),
                                stage);

                return inquiryRepository.save(inquiry).getId();
        }

        // 내 문의 목록 조회
        @Transactional(readOnly = true)
        public Page<InquiryResponseDto> getMyInquiries(Long userId, Pageable pageable) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

                return inquiryRepository.findByUser(user, pageable)
                                .map(this::toInquiryDto);
        }

        // 내 문의 상세 조회 (문의 + 답변)
        @Transactional(readOnly = true)
        public InquiryDetailResponseDto getInquiryDetail(Long inquiryId, Long requestUserId) {
                Inquiry inquiry = inquiryRepository.findById(inquiryId)
                                .orElseThrow(() -> new IllegalArgumentException("문의가 존재하지 않습니다."));

                User requester = userRepository.findById(requestUserId)
                                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

                boolean isOwner = inquiry.getUser().getId().equals(requestUserId);
                boolean isAdmin = requester.getRole() == User.UserRole.ADMIN;

                if (inquiry.isPrivate() && !isOwner && !isAdmin) {
                        throw new IllegalArgumentException("열람 권한이 없습니다.");
                }

                InquiryAnswerResponseDto answer = inquiryAnswerRepository.findByInquiryId(inquiryId)
                                .map(this::toAnswerDto)
                                .orElse(null);

                return InquiryDetailResponseDto.builder()
                                .id(inquiry.getId())
                                .title(inquiry.getTitle())
                                .content(inquiry.getContent())
                                .isAnswered(inquiry.isAnswered())
                                .createdAt(inquiry.getCreatedAt())
                                .answer(answer)
                                .build();
        }

        // 어드민용 검색
        @Transactional(readOnly = true)
        public Page<InquiryResponseDto> searchInquiriesAdmin(String keyword, Boolean isAnswered, Pageable pageable) {
                return inquiryRepository.searchAdmin(keyword, isAnswered, pageable)
                                .map(this::toInquiryDto);
        }

        // 유저용 검색
        @Transactional(readOnly = true)
        public Page<InquiryResponseDto> searchMyInquiries(Long userId, String keyword, Boolean isAnswered,
                        Pageable pageable) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

                return inquiryRepository.searchUser(user, keyword, isAnswered, pageable)
                                .map(this::toInquiryDto);
        }

        // 전체 문의 목록 조회 (관리자)
        @Transactional(readOnly = true)
        public Page<InquiryResponseDto> getAllInquiries(Pageable pageable) {
                return inquiryRepository.findAll(pageable)
                                .map(this::toInquiryDto);
        }

        // 문의에 답변 작성 (관리자)
        @Transactional
        public Long createAnswer(Long inquiryId, Long adminId, InquiryAnswerRequestDto dto) {
                Inquiry inquiry = inquiryRepository.findById(inquiryId)
                                .orElseThrow(() -> new IllegalArgumentException("문의가 존재하지 않습니다."));

                InquiryAnswer answer = InquiryAnswer.create(inquiryId, adminId, dto.getContent());
                inquiryAnswerRepository.save(answer);

                inquiry.markAnswered();

                // 알림 저장
                notificationRepository.save(Notification.create(
                                inquiry.getUser().getId(),
                                "작성하신 문의에 답변이 등록되었습니다."));

                return answer.getId();
        }

        private InquiryResponseDto toInquiryDto(Inquiry inquiry) {
                return InquiryResponseDto.builder()
                                .id(inquiry.getId())
                                .userId(inquiry.getUser().getId())
                                .title(inquiry.getTitle())
                                .content(inquiry.getContent())
                                .isAnswered(inquiry.isAnswered())
                                .createdAt(inquiry.getCreatedAt())
                                .build();
        }

        private InquiryAnswerResponseDto toAnswerDto(InquiryAnswer answer) {
                return InquiryAnswerResponseDto.builder()
                                .id(answer.getId())
                                .inquiryId(answer.getInquiryId())
                                .adminId(answer.getAdminId())
                                .content(answer.getContent())
                                .createdAt(answer.getCreatedAt())
                                .modifiedAt(answer.getModifiedAt())
                                .build();
        }
}
