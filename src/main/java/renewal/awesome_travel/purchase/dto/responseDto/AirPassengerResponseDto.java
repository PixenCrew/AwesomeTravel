package renewal.awesome_travel.purchase.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AirPassengerResponseDto {

    private String name;
    private String number;
    private String email;
    private LocalDate birth;
    private String sex;
    private String nationality;  // 예: "KOR"
    private String passportNum;
    private String lastName;
    private String firstName;
    private LocalDate expire;

    private List<String> specialRequests; // "Wheelchair", "Vegetarian" 등
}

