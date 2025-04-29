package renewal.awesome_travel.inquiry.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InquiryDetailResponseDto {
    private Long id;
    private String title;
    private String content;
    private boolean isAnswered;
    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;
    private InquiryAnswerResponseDto answer;
}

