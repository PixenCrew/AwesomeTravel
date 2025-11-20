package renewal.awesome_travel.config;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.common.entity.User;
import renewal.common.entity.User.MemberGrade;
import renewal.common.entity.User.UserProvider;
import renewal.common.entity.User.UserRole;
import renewal.common.entity.User.UserStatus;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(request);

        // google, naver
        UserProvider provider = UserProvider.valueOf(
                request.getClientRegistration().getRegistrationId().toUpperCase());

        String id;
        String email;
        String name;
        String profileImage;

        if (provider == UserProvider.GOOGLE) { // GOOGLE

            id = oauth2User.getAttribute("sub");
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name");
            profileImage = oauth2User.getAttribute("picture"); // Google 프로필 이미지

        } else if (provider == UserProvider.NAVER) { // NAVER

            Map<String, Object> response = oauth2User.getAttribute("response");

            if (response != null) {

                id = (String) response.get("id");
                email = (String) response.get("email");
                name = (String) response.get("name");
                profileImage = (String) response.get("profile_image"); // Naver 프로필 이미지

            } else {
                throw new OAuth2AuthenticationException("Naver OAuth2 실패");
            }

        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 provider입니다.");
        }

        // 이메일로 유저 찾기
        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    // provider가 다르면 예외 발생
                    if (existingUser.getProvider() != provider) {
                        throw new OAuth2AuthenticationException(
                                "해당 이메일은 " + existingUser.getProvider() + " 계정으로 가입되어 있습니다.");
                    }
                    return new CustomUserDetails(existingUser, oauth2User.getAttributes());
                })
                .orElseGet(() -> {
                    // 신규 회원가입 처리
                    User newUser = registerUser(email, name, provider, id, profileImage);
                    return new CustomUserDetails(newUser, oauth2User.getAttributes());
                });
    }

    private User registerUser(String email, String name, UserProvider provider, String providerId,
            String profileImage) {
        User user = User.builder()
                .email(email)
                .name(name)
                .provider(provider)
                .providerId(providerId)
                .profileImage(profileImage)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .grade(MemberGrade.BASIC)
                .build();

        return userRepository.save(user);
    }

}
