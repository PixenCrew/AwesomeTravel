package renewal.awesome_travel.purchase.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.purchase.dto.CountryDto;
import renewal.awesome_travel.purchase.dto.SpecialRequestDto;
import renewal.awesome_travel.purchase.entity.AirPurchase;
import renewal.awesome_travel.purchase.entity.Country;
import renewal.awesome_travel.purchase.utiles.Sex;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AirPassengerResponseDto {

    private Long id;

    private AirPurchaseResponseDto airPurchase;

    private String name;

    private String number;

    private String email;

    private LocalDate birth;

    private Sex sex;

    private CountryDto nationality;

    private String passport_num;

    private String lastName;

    private String firstName;

    private LocalDate expire;

    private Set<SpecialRequestDto> specialRequests;



}
