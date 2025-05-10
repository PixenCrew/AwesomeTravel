package renewal.awesome_travel.wait.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.product.dto.responseDto.ProductListResponseDto;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WaitResponseDto {

    private Long id;

    private ProductListResponseDto productListResponseDto;

    private MemberResponseDto memberResponseDto;

    private LocalDate wait_date;

    private boolean purchased;
}
