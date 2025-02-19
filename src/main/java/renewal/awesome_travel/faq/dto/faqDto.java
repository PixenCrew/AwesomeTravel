package renewal.awesome_travel.faq.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.faq.utiles.CsCategory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class faqDto {

    private Long id;

    private String title;

    private CsCategory category;

    private String ask;

    private String response;
}
