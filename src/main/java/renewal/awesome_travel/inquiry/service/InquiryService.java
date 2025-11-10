package renewal.awesome_travel.inquiry.service;

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
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.common.entity.Inquiry;
import renewal.common.entity.InquiryAnswer;
import renewal.common.entity.Notification;
import renewal.common.entity.User;

@Service
@RequiredArgsConstructor
public class InquiryService {

        private final InquiryRepository inquiryRepository;
        private final InquiryAnswerRepository inquiryAnswerRepository;
        private final NotificationRepository notificationRepository;
        private final UserRepository userRepository;

        // 문의 작성
        public Long createInquiry(Long userId, InquiryRequestDto dto) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));
                Inquiry inquiry = Inquiry.create(user, dto.getTitle(), dto.getContent(), dto.getCategory());
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
                if (!inquiry.getUser().getId().equals(requestUserId)) {
                        throw new IllegalArgumentException("본인만 조회할 수 있습니다.");
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
