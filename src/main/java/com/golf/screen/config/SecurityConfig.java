package com.golf.screen.config;

import com.golf.screen.config.oauth.OAuth2SuccessHandler;
import com.golf.screen.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API 개발 편의를 위해 CSRF 비활성화 및 모든 요청 허용 설정
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll())
                // 일반 로그인 설정 (API 연동을 위해 성공 시 200, 실패 시 401 응답 처리)
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(200);
                            String sessionId = request.getSession().getId();
                            response.getWriter().write("{\"status\":\"success\",\"token\":\"" + sessionId + "\"}");
                            response.getWriter().flush();
                        })
                        .failureHandler((request, response, exception) -> {
                            System.err.println("로그인 실패 원인: " + exception.getMessage());
                            exception.printStackTrace();
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write("{\"status\":\"fail\",\"message\":\"" + exception.getMessage() + "\"}");
                            response.getWriter().flush();
                        })
                        .permitAll())
                // OAuth2 로그인 설정 추가
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler));
        return http.build();
    }
}
