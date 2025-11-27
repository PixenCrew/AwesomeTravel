package renewal.awesome_travel.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PassportRequestDto {

    private Long id; // 여권 id (신규 생성이면 null)
    private String passportNum;

    private String lastName;
    private String firstName;
    private String lastNameKor;
    private String firstNameKor;

    private String birth;
    private String sex;

    private String countryCode;
    private String nationality;
    private String authority;

    private String issue;
    private String expire;
    
    private String phone; // 전화번호 (User 엔티티 업데이트용)
}
