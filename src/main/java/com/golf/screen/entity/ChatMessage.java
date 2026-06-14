package com.golf.screen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "join_post_id", nullable = false)
    private Long joinPostId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "sender_nickname", nullable = false)
    private String senderNickname;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "send_time", nullable = false)
    private LocalDateTime sendTime;

    @Transient
    private String senderProfileImageUrl;
}
