package renewal.awesome_travel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@EntityScan(basePackages = {
		"renewal.common",
		"renewal.awesome_travel"
})
@ComponentScan(basePackages = {
		"renewal.common",
		"renewal.awesome_travel"
})
@EnableJpaRepositories(basePackages = {
		"renewal.common",
		"renewal.awesome_travel"
})
public class AwesomeTravelApplication {

	public static void main(String[] args) {
		SpringApplication.run(AwesomeTravelApplication.class, args);
	}

}
