package renewal.awesome_travel.passport.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PassportUpdateRequest {
    private List<PassportDto> passengers;
}
