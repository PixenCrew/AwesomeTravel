package renewal.awesome_travel.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.config.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomUserDetailsService customUserDetailsService;
        private final CustomOAuth2UserService customOAuth2UserService;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/", true) // 추후 메인페이지로 바꿔야함
                                                .permitAll())
                                .rememberMe(remember -> remember
                                                .key("awesomeawesomeawesome")
                                                .tokenValiditySeconds(60 * 60 * 24 * 7) // 7일
                                                .rememberMeParameter("remember-me") // checkbox 이름
                                                .rememberMeCookieName("remember-me-cookie") // 쿠키 이름
                                                .userDetailsService(customUserDetailsService))
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login") // 소셜 로그인도 동일한 로그인 페이지 사용
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService)))
                                .logout(logout -> logout
                                                .logoutUrl("/logout") // 로그아웃 URL
                                                .logoutSuccessUrl("/") // 로그아웃 후 이동 경로
                                                .invalidateHttpSession(true) // 세션 무효화
                                                .deleteCookies("JSESSIONID", "remember-me-cookie") // 쿠키 삭제
                                                .permitAll())
                                .authorizeHttpRequests(auth -> auth
                                                .anyRequest().permitAll());
                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // UserDetailsService 등록
        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
                auth.setUserDetailsService(customUserDetailsService);
                auth.setPasswordEncoder(passwordEncoder());
                return auth;
        }
}
