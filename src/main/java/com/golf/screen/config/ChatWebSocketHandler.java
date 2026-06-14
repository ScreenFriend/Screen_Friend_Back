package com.golf.screen.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.golf.screen.entity.ChatMessage;
import com.golf.screen.repository.ChatMessageRepository;
import com.golf.screen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // key: joinPostId, value: 연결된 WebSocketSession 세션 셋
    private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long joinPostId = getJoinPostId(session);
        if (joinPostId != null) {
            roomSessions.computeIfAbsent(joinPostId, k -> new CopyOnWriteArraySet<>()).add(session);
            log.info("WebSocket 연결 성립 - 조인방 ID: {}, 세션 ID: {}", joinPostId, session.getId());
        } else {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long joinPostId = getJoinPostId(session);
        if (joinPostId == null) {
            return;
        }

        String payload = message.getPayload();
        log.info("수신 메시지 - 조인방 ID: {}, 내용: {}", joinPostId, payload);

        try {
            // 수신 데이터 파싱
            ChatMessage incoming = objectMapper.readValue(payload, ChatMessage.class);
            incoming.setJoinPostId(joinPostId);
            incoming.setSendTime(LocalDateTime.now());

            // 1. DB 저장
            chatMessageRepository.save(incoming);

            // 유저 프로필 이미지 세팅
            userRepository.findById(incoming.getSenderId()).ifPresent(user -> {
                incoming.setSenderProfileImageUrl(user.getProfileImageUrl());
            });

            // 2. 해당 방의 다른 세션들로 브로드캐스트
            String jsonResponse = objectMapper.writeValueAsString(incoming);
            TextMessage textResponse = new TextMessage(jsonResponse);

            Set<WebSocketSession> sessions = roomSessions.get(joinPostId);
            if (sessions != null) {
                for (WebSocketSession s : sessions) {
                    if (s.isOpen()) {
                        try {
                            s.sendMessage(textResponse);
                        } catch (IOException e) {
                            log.error("메시지 브로드캐스트 실패 - 세션 ID: {}", s.getId(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("웹소켓 메시지 처리 중 오류 발생", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long joinPostId = getJoinPostId(session);
        if (joinPostId != null) {
            Set<WebSocketSession> sessions = roomSessions.get(joinPostId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    roomSessions.remove(joinPostId);
                }
            }
            log.info("WebSocket 연결 종료 - 조인방 ID: {}, 세션 ID: {}", joinPostId, session.getId());
        }
    }

    // URI 경로 '/ws/chat/{joinPostId}'에서 joinPostId 추출
    private Long getJoinPostId(WebSocketSession session) {
        try {
            String path = session.getUri().getPath();
            String[] segments = path.split("/");
            // 기대하는 path: /ws/chat/123 -> segments: ["", "ws", "chat", "123"]
            String idStr = segments[segments.length - 1];
            return Long.parseLong(idStr);
        } catch (Exception e) {
            log.error("웹소켓 세션에서 조인방 ID 추출 실패 - URI: {}", session.getUri(), e);
            return null;
        }
    }

    public void broadcastSystemMessage(Long joinPostId, ChatMessage message) {
        try {
            String jsonResponse = objectMapper.writeValueAsString(message);
            TextMessage textResponse = new TextMessage(jsonResponse);
            Set<WebSocketSession> sessions = roomSessions.get(joinPostId);
            if (sessions != null) {
                for (WebSocketSession s : sessions) {
                    if (s.isOpen()) {
                        try {
                            s.sendMessage(textResponse);
                        } catch (IOException e) {
                            log.error("시스템 메시지 전송 실패 - 세션 ID: {}", s.getId(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("시스템 메시지 브로드캐스트 중 에러 발생", e);
        }
    }
}
