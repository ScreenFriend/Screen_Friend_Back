package com.golf.screen.controller;

import com.golf.screen.entity.ChatMessage;
import com.golf.screen.repository.ChatMessageRepository;
import com.golf.screen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/joins")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    /**
     * 특정 조인방의 이전 채팅 메시지 내역 조회
     */
    @GetMapping("/{joinPostId}/chats")
    public ResponseEntity<List<ChatMessage>> getChatMessages(@PathVariable Long joinPostId) {
        List<ChatMessage> chats = chatMessageRepository.findByJoinPostIdOrderBySendTimeAsc(joinPostId);
        for (ChatMessage chat : chats) {
            userRepository.findById(chat.getSenderId()).ifPresent(user -> {
                chat.setSenderProfileImageUrl(user.getProfileImageUrl());
            });
        }
        return ResponseEntity.ok(chats);
    }
}
