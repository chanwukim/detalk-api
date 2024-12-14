package net.detalk.api.support;

import lombok.RequiredArgsConstructor;
import net.detalk.api.support.security.HasRoleArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final HasRoleArgumentResolver hasRoleArgumentResolver;
    private final Environment env;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins;

        if (env.getActiveProfiles().length > 0 && env.getActiveProfiles()[0].equals("prod")) {
            allowedOrigins = new String[]{"https://detalk.net.com", "http://www.detalk.net.com"};
        } else {
            allowedOrigins = new String[]{"http://localhost:3000"};
        }

        registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(hasRoleArgumentResolver);
    }
}
