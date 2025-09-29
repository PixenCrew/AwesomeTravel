package renewal.awesome_travel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${iconLocation}")
    private String iconDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 항공사 아이콘 (공용)
        String iconPath = "file:" + iconDir + "/";
        registry.addResourceHandler("/icon/**")
                .addResourceLocations(iconPath);
    }
}
