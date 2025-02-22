package renewal.awesome_travel.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QnAProductDto {

    private Long memberId;

    private Long productId;

    private String content;
}
