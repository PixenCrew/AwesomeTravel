package renewal.awesome_travel.purchase.dto.requestDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.common.entity.CountryCode;
import renewal.common.entity.BasePassenger.Sex;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class AirPassengerUpdateRequestDto {

    private String name;
    private String number;
    private String email;

    private LocalDate birth;
    private Sex sex;

    private CountryCode countryCode;         // 국적
    private String passportNum;
    private String lastName;
    private String firstName;
    private LocalDate expire;

    private List<Long> specialRequestIds; // 요청사항 (선택적으로 수정 가능)
}

