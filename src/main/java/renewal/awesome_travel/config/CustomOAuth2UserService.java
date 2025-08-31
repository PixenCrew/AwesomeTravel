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
import renewal.awesome_travel.user.utils.Provider;
import renewal.awesome_travel.user.utils.Role;
import renewal.awesome_travel.user.utils.Status;
import renewal.common.entity.User;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(request);

        String provider = request.getClientRegistration().getRegistrationId(); // google, naver, kakao
        String providerId = oauth2User.getName();
        String email = getEmailFromAttributes(provider, oauth2User.getAttributes());

        // 이메일로 유저 찾기
        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    // provider가 다르면 예외 발생
                    if (!existingUser.getProvider().name().equalsIgnoreCase(provider)) {
                        throw new OAuth2AuthenticationException("해당 이메일은 " + existingUser.getProvider() + " 계정으로 가입되어 있습니다.");
                    }
                    return new CustomUserDetails(existingUser, oauth2User.getAttributes());
                })
                .orElseGet(() -> {
                    // 신규 회원가입 처리
                    User newUser = registerUser(email, provider, providerId);
                    return new CustomUserDetails(newUser, oauth2User.getAttributes());
                });
    }

    private String getEmailFromAttributes(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "google" -> (String) attributes.get("email");
            case "naver" -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                yield (String) response.get("email");
            }
            case "kakao" -> {
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                yield (String) kakaoAccount.get("email");
            }
            default -> throw new IllegalArgumentException("지원하지 않는 provider: " + provider);
        };
    }

    private User registerUser(String email, String provider, String providerId) {
        User user = User.builder()
                .email(email)
                .provider(Provider.valueOf(provider.toUpperCase()))
                .providerId(providerId)
                .role(Role.USER)
                .status(Status.ACTIVE)
                .emailVerified(true)
                .build();

        return userRepository.save(user);
    }
}
