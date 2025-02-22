package renewal.awesome_travel.wait.dto.request;

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
public class WaitRequestDto {

    private Long productList_id;

    private Long member_id;

    private LocalDate wait_date;
}
