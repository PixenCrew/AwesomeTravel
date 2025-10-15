package renewal.awesome_travel.faq.dto.response;

import lombok.Builder;
import lombok.Getter;
import renewal.common.entity.Faq.FaqCategory;

import java.time.LocalDateTime;

@Getter
@Builder
public class FaqResponseDto {
    Long id;
    String question;
    String answer;
    FaqCategory category;
    LocalDateTime createdAt;
}
