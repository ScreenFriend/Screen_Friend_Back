package com.golf.screen.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Spring Security Filter Chain보다 먼저 동작하도록 최우선 순위 설정
public class SessionHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authToken = httpRequest.getHeader("X-Auth-Token");

        if (authToken != null && !authToken.trim().isEmpty()) {
            // 헤더로 수신된 토큰을 톰캣 서블릿 세션 ID로 매핑하도록 HttpServletRequestWrapper 적용
            HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(httpRequest) {
                @Override
                public String getRequestedSessionId() {
                    return authToken;
                }

                @Override
                public boolean isRequestedSessionIdValid() {
                    return true;
                }
            };
            chain.doFilter(wrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
