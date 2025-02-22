package renewal.awesome_travel.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QnaProductListDto {
    private Long memberId;

    private Long productListId;

    private String content;
}
