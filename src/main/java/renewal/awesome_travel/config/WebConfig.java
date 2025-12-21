package renewal.awesome_travel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

//     @Value("${iconLocation}")
//     private String iconDir;

    @Value("${profileImageLocation}")
    private String profileImageDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
//         // 항공사 아이콘 (공용)
//         String iconPath = "file:" + iconDir + "/";
//         registry.addResourceHandler("/icon/**")
//                 .addResourceLocations(iconPath);
        
        // 프로필 이미지
        String profilePath = "file:" + profileImageDir + "/";
        registry.addResourceHandler("/images/profile/**")
                .addResourceLocations(profilePath);
    }
}
