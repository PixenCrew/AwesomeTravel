package renewal.awesome_travel;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.awesome_travel.user.dto.response.UserResponseDto;
import renewal.awesome_travel.user.service.UserService;
import renewal.common.entity.User;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserService userService;

    @ModelAttribute("currentUser")
    public UserResponseDto currentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal != null) {
            User user = principal.getUser(); // 엔티티 가져오기
            // UserResponseDTO로 변환
            return new UserResponseDto(user);
        }
        return null; // 로그인 안 된 경우
    }

    @ModelAttribute("likedProductsCount")
    public Integer likedProductsCount(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal != null && principal.getUser() != null && principal.getUser().getId() != null) {
            // UserService를 통해 세션 내에서 초기화하여 조회
            return userService.getLikedProductsCount(principal.getUser().getId());
        }
        return 0;
    }

    @ModelAttribute("userCouponsCount")
    public Integer userCouponsCount(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal != null) {
            User user = principal.getUser();
            return userService.getAvailableCoupons(user).size();
        }
        return 0;
    }
}
