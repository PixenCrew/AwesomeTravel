package renewal.awesome_travel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@Configuration
public class JpaConfig {

    //추후 시큐리티 구현
//    @Bean
//    public AuditorAware<String> auditorAware() {
//        return new AuditorAwareImpl();
//    }

}
