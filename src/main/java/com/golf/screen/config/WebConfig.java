package com.golf.screen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 로컬 업로드 디렉토리 절대 경로 확보
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absolutePath);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 프론트엔드 개발 서버(예: React, Vue, Flutter Web 등)에서의 원활한 API 호출을 위해 CORS 허용 설정을 추가합니다.
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 실무 배포 시에는 특정 프론트 도메인만 지정합니다.
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
