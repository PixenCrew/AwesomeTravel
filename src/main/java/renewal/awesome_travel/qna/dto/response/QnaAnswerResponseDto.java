package renewal.awesome_travel.qna.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QnaAnswerResponseDto {
    private Long id;
    private Long qnaId;
    private Long responderId;
    private String content;
    private LocalDateTime createdAt;
}

