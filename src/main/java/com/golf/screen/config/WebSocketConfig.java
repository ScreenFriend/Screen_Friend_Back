package com.golf.screen.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 엔드포인트 `/ws/chat/{joinPostId}`에 핸들러 등록 및 CORS 전면 허용
        registry.addHandler(chatWebSocketHandler, "/ws/chat/*")
                .setAllowedOrigins("*");
    }
}
