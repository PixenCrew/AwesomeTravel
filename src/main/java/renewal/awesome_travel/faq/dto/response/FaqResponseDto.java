package renewal.awesome_travel.faq.dto.response;

import lombok.Builder;
import lombok.Getter;
import renewal.awesome_travel.faq.utils.FaqCategory;

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
