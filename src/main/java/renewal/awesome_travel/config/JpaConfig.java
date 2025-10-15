package renewal.awesome_travel.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() 
                || authentication instanceof AnonymousAuthenticationToken) {
                return Optional.of("system"); // 인증 정보 없으면 기본값 사용
            }

            return Optional.of(authentication.getName());
        };
    }
}
