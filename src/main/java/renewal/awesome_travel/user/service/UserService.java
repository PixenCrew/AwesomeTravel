package renewal.awesome_travel.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import renewal.awesome_travel.user.dto.request.PasswordChangeRequestDto;
import renewal.awesome_travel.user.dto.request.UserRegisterRequestDto;
import renewal.awesome_travel.user.dto.request.UserUpdateRequestDto;
import renewal.awesome_travel.user.dto.response.EmailCheckResponseDto;
import renewal.awesome_travel.user.dto.response.UserResponseDto;
import renewal.awesome_travel.user.entity.EmailVerificationToken;
import renewal.awesome_travel.user.repository.EmailVerificationTokenRepository;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.awesome_travel.user.utils.Provider;
import renewal.awesome_travel.user.utils.Role;
import renewal.awesome_travel.user.utils.Status;
import renewal.common.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailVerificationTokenRepository tokenRepository;

    private final EmailService emailService;

    @Transactional
    public Long register(UserRegisterRequestDto dto) {
        Optional<User> existing = userRepository.findByEmail(dto.getEmail());

        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getProvider() != Provider.LOCAL) {
                // 이미 소셜 로그인으로 가입된 이메일
                throw new IllegalArgumentException("해당 이메일은 " + user.getProvider() + " 계정으로 이미 가입되어 있습니다.");
            }
            // 동일 이메일로 LOCAL 가입자가 이미 있는 경우
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 신규 가입 진행
        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .role(Role.USER)
                .provider(Provider.LOCAL)
                .status(Status.ACTIVE)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        EmailVerificationToken token = EmailVerificationToken.create(user);
        tokenRepository.save(token);

        emailService.sendVerificationMail(user.getEmail(), token.getToken());

        return user.getId();
    }


    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken evt = tokenRepository.findById(token)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 인증 링크입니다."));

        if (evt.isExpired()) {
            throw new IllegalStateException("인증 링크가 만료되었습니다.");
        }

        User user = evt.getUser();
        user.setEmailVerified(true);
        tokenRepository.delete(evt); // 인증 성공 시 토큰 제거
    }

    //이메일 중복 확인
    public EmailCheckResponseDto checkEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return EmailCheckResponseDto.builder()
                    .exists(false)
                    .provider(null)
                    .message("사용 가능한 이메일입니다.")
                    .build();
        }

        Provider provider = userOpt.get().getProvider();

        String msg = switch (provider) {
            case LOCAL -> "이미 가입된 이메일입니다.";
            case GOOGLE -> "해당 이메일은 구글 계정으로 가입되어 있습니다.";
            case NAVER -> "해당 이메일은 네이버 계정으로 가입되어 있습니다.";
            case KAKAO -> "해당 이메일은 카카오 계정으로 가입되어 있습니다.";
        };

        return EmailCheckResponseDto.builder()
                .exists(true)
                .provider(provider.name())
                .message(msg)
                .build();
    }






    @Transactional(readOnly = true)
    public UserResponseDto getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new UserResponseDto(user);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    @Transactional
    public void updateUserInfo(Long userId, UserUpdateRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getBirthDate() != null) user.setBirthDate(dto.getBirthDate());

        if (dto.getPassportNumber() != null) user.setPassportNumber(dto.getPassportNumber());
        if (dto.getPassportIssuedDate() != null) user.setPassportIssuedDate(dto.getPassportIssuedDate());
        if (dto.getPassportExpiryDate() != null) user.setPassportExpiryDate(dto.getPassportExpiryDate());
        if (dto.getPassportCountry() != null) user.setPassportCountry(dto.getPassportCountry());
        if (dto.getEnglishFirstName() != null) user.setEnglishFirstName(dto.getEnglishFirstName());
        if (dto.getEnglishLastName() != null) user.setEnglishLastName(dto.getEnglishLastName());

        if (dto.getMarketingConsent() != null) user.setMarketingConsent(dto.getMarketingConsent());
    }




    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setStatus(Status.WITHDRAWN);
    }


}
