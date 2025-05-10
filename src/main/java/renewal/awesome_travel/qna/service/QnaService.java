package renewal.awesome_travel.qna.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import renewal.awesome_travel.qna.dto.request.QnaAnswerRequestDto;
import renewal.awesome_travel.qna.dto.request.QnaAnswerUpdateRequestDto;
import renewal.awesome_travel.qna.dto.request.QnaRequestDto;
import renewal.awesome_travel.qna.dto.request.QnaUpdateRequestDto;
import renewal.awesome_travel.qna.dto.response.QnaAnswerResponseDto;
import renewal.awesome_travel.qna.dto.response.QnaDetailResponseDto;
import renewal.awesome_travel.qna.dto.response.QnaResponseDto;
import renewal.awesome_travel.qna.entity.Qna;
import renewal.awesome_travel.qna.entity.QnaAnswer;
import renewal.awesome_travel.qna.repository.QnaAnswerRepository;
import renewal.awesome_travel.qna.repository.QnaRepository;
import renewal.awesome_travel.user.entity.User;
import renewal.awesome_travel.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaRepository qnaRepository;
    private final QnaAnswerRepository qnaAnswerRepository;
    private final UserRepository userRepository;

    // 질문 등록
    public Long createQna(Long writerId, QnaRequestDto dto) {
        User writer = userRepository.findById(writerId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        Qna qna = Qna.create(writer, dto.getTitle(), dto.getContent());
        return qnaRepository.save(qna).getId();
    }


    // 전체 질문 조회
    public Page<QnaResponseDto> getAllQna(Pageable pageable) {
        return qnaRepository.findAll(pageable)
                .map(this::toQnaDto);
    }

    // 답변 여부로 필터 조회 (관리자용)
    public Page<QnaResponseDto> getAllQnaAdmin(Boolean isAnswered, Pageable pageable) {
        if (isAnswered == null) {
            return qnaRepository.findAll(pageable)
                    .map(this::toQnaDto);
        }
        return qnaRepository.findByIsAnswered(isAnswered, pageable)
                .map(this::toQnaDto);
    }

    // 질문 상세 조회 (질문 + 답변 리스트)
    public QnaDetailResponseDto getQna(Long id) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("QnA가 존재하지 않습니다."));
        List<QnaAnswerResponseDto> answers = qnaAnswerRepository.findByQnaId(id).stream()
                .map(this::toAnswerDto)
                .toList();

        return QnaDetailResponseDto.builder()
                .id(qna.getId())
                .writerId(qna.getWriter().getId())
                .title(qna.getTitle())
                .content(qna.getContent())
                .isAnswered(qna.isAnswered())
                .createdAt(qna.getCreatedAt())
                .answers(answers)
                .build();
    }

    // 질문 수정 (본인만)
    @Transactional
    public void updateQnaPartial(Long qnaId, Long requestUserId, QnaUpdateRequestDto dto) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("QnA가 존재하지 않습니다."));
        if (!qna.getWriter().getId().equals(requestUserId)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }
        if (dto.getTitle() != null) qna.updateTitle(dto.getTitle());
        if (dto.getContent() != null) qna.updateContent(dto.getContent());
    }

    // 질문 삭제 (본인만)
    @Transactional
    public void deleteQna(Long qnaId, Long requestUserId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("QnA가 존재하지 않습니다."));
        if (!qna.getWriter().getId().equals(requestUserId)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }
        qnaRepository.delete(qna);
    }

    // 답변 등록 (관리자만, @PreAuthorize로 제한)
    @Transactional
    public Long createAnswer(Long qnaId, Long responderId, QnaAnswerRequestDto dto) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("QnA가 존재하지 않습니다."));

        QnaAnswer answer = QnaAnswer.create(qnaId, responderId, dto.getContent());
        qnaAnswerRepository.save(answer);

        qna.markAnswered();
        return answer.getId();
    }

    // 답변 수정 (관리자만)
    @Transactional
    public void updateAnswerPartial(Long answerId, QnaAnswerUpdateRequestDto dto) {
        QnaAnswer answer = qnaAnswerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("답변이 존재하지 않습니다."));
        if (dto.getContent() != null) answer.updateContent(dto.getContent());
    }

    @Transactional(readOnly = true)
    public Page<QnaResponseDto> searchQna(String keyword, Pageable pageable) {
        return qnaRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable)
                .map(this::toQnaDto);
    }


    // 답변 삭제 (관리자만)
    @Transactional
    public void deleteAnswer(Long answerId) {
        qnaAnswerRepository.deleteById(answerId);
    }

    private QnaResponseDto toQnaDto(Qna qna) {
        return QnaResponseDto.builder()
                .id(qna.getId())
                .writerId(qna.getWriter().getId())
                .title(qna.getTitle())
                .content(qna.getContent())
                .isAnswered(qna.isAnswered())
                .createdAt(qna.getCreatedAt())
                .build();
    }

    private QnaAnswerResponseDto toAnswerDto(QnaAnswer answer) {
        return QnaAnswerResponseDto.builder()
                .id(answer.getId())
                .qnaId(answer.getQnaId())
                .responderId(answer.getResponderId())
                .content(answer.getContent())
                .createdAt(answer.getCreatedAt())
                .build();
    }
}


