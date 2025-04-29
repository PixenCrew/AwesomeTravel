package renewal.awesome_travel.qna.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QnaResponseDto {
    private Long id;
    private Long writerId;
    private String title;
    private String content;
    private boolean isAnswered;
    private LocalDateTime createdAt;
}
