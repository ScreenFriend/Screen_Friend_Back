package com.golf.screen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "join_posts", indexes = {
    @Index(name = "idx_join_posts_dong", columnList = "dong")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class JoinPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, name = "golf_center_name")
    private String golfCenterName;

    @Column(nullable = false, name = "play_date_time")
    private LocalDateTime playDateTime;

    @Column(nullable = false, name = "max_players")
    private int maxPlayers;

    @Column(nullable = false, name = "current_players")
    private int currentPlayers;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name = "dong")
    private String dong;

    @Column(name = "is_reserved", nullable = false)
    private boolean isReserved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, columnDefinition = "VARCHAR(30) DEFAULT 'DUTCH_PAY'")
    @Builder.Default
    private PaymentType paymentType = PaymentType.DUTCH_PAY;

    @Column(name = "golf_center_phone")
    private String golfCenterPhone;
}
