package renewal.awesome_travel.faq.dto.request;

import lombok.Getter;
import lombok.Setter;
import renewal.awesome_travel_backoffice.faq.utils.FaqCategory;

@Getter
@Setter
public class FaqRequestDto {
    private String question;
    private String answer;
    private FaqCategory category;
}
