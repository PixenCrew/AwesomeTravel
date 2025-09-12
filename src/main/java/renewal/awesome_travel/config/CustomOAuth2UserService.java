package renewal.awesome_travel.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.common.entity.User.UserProvider;
import renewal.common.entity.User.UserRole;
import renewal.common.entity.User.UserStatus;
import renewal.common.entity.User;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(request);

        // google, naver, kakao
        UserProvider provider = UserProvider.valueOf(
                request.getClientRegistration().getRegistrationId().toUpperCase());

        String id;
        String email;
        String name;

        if (provider == UserProvider.GOOGLE) { // GOOGLE

            id = oauth2User.getAttribute("sub");
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name");

        } else if (provider == UserProvider.NAVER) { // NAVER

            Map<String, Object> response = oauth2User.getAttribute("response");

            if (response != null) {

                id = (String) response.get("id");
                email = (String) response.get("email");
                name = (String) response.get("name");

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
                    if ( existingUser.getProvider() != provider ) {
                        throw new OAuth2AuthenticationException(
                                "해당 이메일은 " + existingUser.getProvider() + " 계정으로 가입되어 있습니다.");
                    }
                    return new CustomUserDetails(existingUser, oauth2User.getAttributes());
                })
                .orElseGet(() -> {
                    // 신규 회원가입 처리
                    User newUser = registerUser(email, name, provider, id);
                    return new CustomUserDetails(newUser, oauth2User.getAttributes());
                });
    }

    private User registerUser(String email, String name, UserProvider provider, String providerId) {
        User user = User.builder()
                .email(email)
                .name(name)
                .provider(provider)
                .providerId(providerId)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();

        return userRepository.save(user);
    }
}
