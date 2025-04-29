package renewal.awesome_travel.qna.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class QnaDetailResponseDto {
    private Long id;
    private String title;
    private String content;
    private Long writerId;
    private boolean isAnswered;
    private LocalDateTime createdAt;
    private List<QnaAnswerResponseDto> answers;
}

