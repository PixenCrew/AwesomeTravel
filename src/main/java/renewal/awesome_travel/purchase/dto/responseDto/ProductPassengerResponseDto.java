package renewal.awesome_travel.purchase.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.purchase.dto.CountryDto;
import renewal.awesome_travel.purchase.dto.requestDto.ProductPurchaseRequestDto;
import renewal.awesome_travel.purchase.dto.SpecialRequestDto;
import renewal.awesome_travel.purchase.utiles.Sex;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductPassengerResponseDto {

    private Long id;

    private ProductPurchaseResponseDto productPurchase; //패키지구매

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
