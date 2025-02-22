package renewal.awesome_travel.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.coupon.utiles.Grade;
import renewal.awesome_travel.passport.dto.response.PassportResponseDto;
import renewal.awesome_travel.wait.dto.response.WaitResponseDto;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDto {
    private String email;
    private String name;
    private String number;
    private Grade grade;
    private Integer point;
    private PassportResponseDto passport;
    private List<WaitResponseDto> waitList;
}
