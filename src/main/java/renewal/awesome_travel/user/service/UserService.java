package renewal.awesome_travel.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import renewal.awesome_travel.payment.repository.PaymentRepository;
import renewal.awesome_travel.user.dto.MemberGradeStatsDto;
import renewal.awesome_travel.user.dto.request.PasswordChangeRequestDto;
import renewal.awesome_travel.user.dto.request.UserRegisterRequestDto;
import renewal.awesome_travel.user.dto.request.UserUpdateRequestDto;
import renewal.awesome_travel.user.dto.response.EmailCheckResponseDto;
import renewal.awesome_travel.user.dto.response.UserResponseDto;
import renewal.awesome_travel.user.entity.EmailVerificationToken;
import renewal.awesome_travel.user.entity.UserCoupon;
import renewal.awesome_travel.user.entity.UserLikedProduct;
import renewal.awesome_travel.user.entity.UserRecentProduct;
import renewal.awesome_travel.user.repository.EmailVerificationTokenRepository;
import renewal.awesome_travel.user.repository.UserCouponRepository;
import renewal.awesome_travel.user.repository.UserLikedProductRepository;
import renewal.awesome_travel.user.repository.UserRecentProductRepository;
import renewal.awesome_travel.user.repository.UserRepository;
import renewal.common.entity.MemberGradeRule;
import renewal.common.entity.Payment;
import renewal.common.entity.User;
import renewal.common.entity.User.MemberGrade;
import renewal.common.entity.User.UserProvider;
import renewal.common.entity.User.UserRole;
import renewal.common.entity.User.UserStatus;
import renewal.common.repository.MemberGradeRuleRepository;
import renewal.common.service.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailVerificationTokenRepository tokenRepository;

    private final EmailService emailService;

    private final UserCouponRepository userCouponRepo;
    private final UserLikedProductRepository userLikedProductRepo;
    private final UserRecentProductRepository userRecentProductRepo;

    @Transactional
    public Long register(UserRegisterRequestDto dto) {
        Optional<User> existing = userRepository.findByEmail(dto.getEmail());

        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getProvider() != UserProvider.LOCAL) {
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
                .role(UserRole.USER)
                .provider(UserProvider.LOCAL)
                .status(UserStatus.INACTIVE) // 이메일 인증 완료 전까진 INACTIVE
                .emailVerified(false)
                .grade(MemberGrade.BASIC)
                .point(0L)
                .terms(dto.getTerms())
                // .createdAt(LocalDateTime.now()) // 생성시간은 JPA Auditing으로 지정
                .build();
        userRepository.save(user);

        // 이메일 발송
        EmailVerificationToken token = EmailVerificationToken.create(user);
        tokenRepository.save(token);
        scheduleVerificationEmail(user.getEmail(), token.getToken());

        return user.getId();
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입 내역이 확인되지 않는 이메일입니다."));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("이미 이메일 인증이 완료된 계정입니다.");
        }

        tokenRepository.deleteAllByUser(user);
        tokenRepository.flush();

        EmailVerificationToken token = EmailVerificationToken.create(user);
        tokenRepository.save(token);
        scheduleVerificationEmail(user.getEmail(), token.getToken());
    }

    @Transactional
    public boolean verifyEmail(String token) {
        try {
            EmailVerificationToken evt = tokenRepository.findById(token)
                    .orElseThrow(() -> new IllegalArgumentException("잘못된 인증 링크입니다."));

            if (evt.isExpired()) {
                // 만료시 토큰 제거
                tokenRepository.delete(evt);
                return false;
            }

            User user = evt.getUser();
            user.setEmailVerified(true);
            user.setStatus(UserStatus.ACTIVE);
            tokenRepository.delete(evt); // 인증 성공 시 토큰 제거

            return true;

        } catch (Exception e) {
            System.out.println("[verifyEmail] error: " + e.getMessage());
            return false;
        }
    }

    private void scheduleVerificationEmail(String email, String token) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendVerificationMailSafely(email, token);
                }
            });
        } else {
            sendVerificationMailSafely(email, token);
        }
    }

    private void sendVerificationMailSafely(String email, String token) {
        try {
            emailService.sendVerificationMail(email, token, "/register/email?token=");
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", email, e);
        }
    }

    // 이메일 중복 확인
    public EmailCheckResponseDto checkEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return EmailCheckResponseDto.builder()
                    .exists(false)
                    .provider(null)
                    .message("사용 가능한 이메일입니다.")
                    .build();
        }

        UserProvider provider = userOpt.get().getProvider();

        String msg = switch (provider) {
            case LOCAL -> "이미 가입된 이메일입니다.";
            case GOOGLE -> "해당 이메일은 구글 계정으로 가입되어 있습니다.";
            case NAVER -> "해당 이메일은 네이버 계정으로 가입되어 있습니다.";
        };

        return EmailCheckResponseDto.builder()
                .exists(true)
                .provider(provider.name())
                .message(msg)
                .build();
    }

    // 이름(닉네임) 중복 확인
    public boolean checkName(String name) {
        return userRepository.existsByName(name);
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

        if (dto.getName() != null)
            user.setName(dto.getName());
        if (dto.getPhone() != null)
            user.setPhone(dto.getPhone());
        if (dto.getBirthDate() != null)
            user.setBirthDate(dto.getBirthDate());
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setStatus(UserStatus.WITHDRAWN);
        userRepository.save(user); // 명시적으로 저장
    }

    // 최근 본 상품 Top N
    public List<UserRecentProduct> getRecentProducts(User user, int limit) {
        return userRecentProductRepo.findTop20ByUserOrderByViewedAtDesc(user)
                .stream().limit(limit).toList();
    }

    // 찜한 상품
    public List<UserLikedProduct> getLikedProducts(User user) {
        return userLikedProductRepo.findByUserAndActiveTrueOrderByLikedAtDesc(user);
    }

    // 찜한 상품 개수 (ElementCollection 사용)
    @Transactional(readOnly = true)
    public int getLikedProductsCount(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            // ElementCollection 초기화
            org.hibernate.Hibernate.initialize(user.getLikedProducts());
            int count = user.getLikedProducts() != null ? user.getLikedProducts().size() : 0;
            log.debug("getLikedProductsCount for userId {}: {}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("Error getting liked products count for userId {}", userId, e);
            return 0;
        }
    }

    // 사용 가능한 쿠폰
    public List<UserCoupon> getAvailableCoupons(User user) {
        return userCouponRepo.findByUserAndUsedFalseAndCoupon_ValidUntilAfter(user, LocalDateTime.now());
    }

    private final MemberGradeRuleRepository ruleRepo;
    private final PaymentRepository paymentRepo;

    public MemberGradeStatsDto evaluate(User user) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneYearAgo = now.minusYears(1);
        LocalDateTime fiveYearsAgo = now.minusYears(5);

        // 결제 목록 2개만 조회 (최적화)
        List<Payment> paymentsLast1Year = paymentRepo.findByUserAndPurchaseStatusAndPurchaseDateBetween(
                user, Payment.PaymentStatus.PAID, oneYearAgo, now);

        List<Payment> paymentsLast5Years = paymentRepo.findByUserAndPurchaseStatusAndPurchaseDateBetween(
                user, Payment.PaymentStatus.PAID, fiveYearsAgo, now);

        int count1Year = paymentsLast1Year.size();
        int count5Years = paymentsLast5Years.size();

        int maxPrice = paymentsLast5Years.stream()
                .mapToInt(p -> p.getPrice().intValue())
                .max().orElse(0);

        int totalPrice5Years = paymentsLast5Years.stream()
                .mapToInt(p -> p.getPrice().intValue())
                .sum();

        // 등급 계산
        MemberGrade grade = applyRules(count1Year, count5Years, maxPrice, totalPrice5Years);

        return new MemberGradeStatsDto(grade, count1Year, count5Years, maxPrice, totalPrice5Years);
    }

    private MemberGrade applyRules(int count1, int count5, int maxPrice, int totalPrice) {

        List<MemberGradeRule> rules = ruleRepo.findAllByOrderByPriorityAsc();

        for (MemberGradeRule rule : rules) {
            if (matches(rule, count1, count5, maxPrice, totalPrice)) {
                return rule.getGrade();
            }
        }

        return MemberGrade.BASIC;
    }

    private boolean matches(MemberGradeRule rule,
            int count1Year,
            int count5Years,
            int maxPrice,
            int totalPrice5Years) {

        if (rule.getMinUseCountLast1Year() != null &&
                count1Year < rule.getMinUseCountLast1Year())
            return false;

        if (rule.getMinUseCountLast5Years() != null &&
                count5Years < rule.getMinUseCountLast5Years())
            return false;

        if (rule.getMinMaxPrice() != null &&
                maxPrice < rule.getMinMaxPrice())
            return false;

        if (rule.getMinTotalPriceLast5Years() != null &&
                totalPrice5Years < rule.getMinTotalPriceLast5Years())
            return false;

        return true;
    }

}
