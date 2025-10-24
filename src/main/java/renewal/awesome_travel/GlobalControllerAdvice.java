package renewal.awesome_travel;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.awesome_travel.user.dto.response.UserResponseDto;
import renewal.common.entity.User;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("currentUser")
    public UserResponseDto currentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal != null) {
            User user = principal.getUser(); // 엔티티 가져오기
            // UserResponseDTO로 변환
            return new UserResponseDto(user);
        }
        return null; // 로그인 안 된 경우
    }
}
