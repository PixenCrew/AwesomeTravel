package renewal.awesome_travel.faq.dto.request;

import lombok.Getter;
import lombok.Setter;
import renewal.common.entity.Faq.FaqCategory;

@Getter
@Setter
public class FaqRequestDto {
    private String question;
    private String answer;
    private FaqCategory category;
}
