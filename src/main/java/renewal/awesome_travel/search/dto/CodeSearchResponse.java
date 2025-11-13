package renewal.awesome_travel.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodeSearchResponse {

    private String cityCode;
    private String cityKor;

    private String airportCode;
    private String airportKor;

}
