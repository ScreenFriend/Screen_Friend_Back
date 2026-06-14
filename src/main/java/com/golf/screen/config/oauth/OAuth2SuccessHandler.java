package com.golf.screen.config.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        System.out.println("OAuth2 소셜 로그인 성공: " + authentication.getName());
        
        String sessionId = request.getSession().getId();
        
        // 절대 경로로 리다이렉트 URL을 명시하여 WebView 리다이렉션 트래킹 유실 방지
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        
        String targetUrl = scheme + "://" + serverName + ":" + serverPort + "/?token=" + sessionId;
        System.out.println("소셜 로그인 리다이렉션 타겟 URL: " + targetUrl);
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
