package renewal.awesome_travel.inquiry.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InquiryAnswerResponseDto {
    private Long id;
    private Long inquiryId;
    private Long adminId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}

