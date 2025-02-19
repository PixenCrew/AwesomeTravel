package renewal.awesome_travel.purchase.dto.requestDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.purchase.dto.SpecialRequestDto;
import renewal.awesome_travel.purchase.entity.Country;
import renewal.awesome_travel.purchase.utiles.Sex;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AirPassengerRequestDto {

    private Long air_purchase_id; //항공구매

    private String name;

    private String number;

    private String email;

    private LocalDate birth;

    private Sex sex;

    private String countryName; //국적

    private String passport_num;

    private String lastName;

    private String firstName;

    private LocalDate expire;

    private Set<SpecialRequestDto> specialRequests;


}
