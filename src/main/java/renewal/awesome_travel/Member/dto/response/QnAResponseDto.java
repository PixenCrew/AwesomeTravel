package renewal.awesome_travel.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QnAResponseDto {

    private Long id;

    private String content;

    private String answer;

    private LocalDateTime date;

    private LocalDateTime answer_date;

    private String memberName;

    private String productName;

}
