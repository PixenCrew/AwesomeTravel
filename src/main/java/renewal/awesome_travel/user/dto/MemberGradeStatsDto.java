package renewal.awesome_travel.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import renewal.common.entity.User.MemberGrade;

@Getter
@AllArgsConstructor
public class MemberGradeStatsDto {
    private MemberGrade grade;
    private int count1Year;
    private int count5Years;
    private int maxPrice;
    private int totalPrice5Years;
}
