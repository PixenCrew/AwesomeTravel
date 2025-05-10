package renewal.awesome_travel.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordChangeRequestDto {
    @NotBlank
    private String currentPassword;
    @NotBlank private String newPassword;
}

