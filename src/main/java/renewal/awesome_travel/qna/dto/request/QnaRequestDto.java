package renewal.awesome_travel.qna.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QnaRequestDto {
    private String title;
    private String content;
}
