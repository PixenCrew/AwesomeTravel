package renewal.awesome_travel.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserRegisterRequestDto {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String name;
}

