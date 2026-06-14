package com.golf.screen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "manner_reviews",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"join_post_id", "reviewer_id", "target_user_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MannerReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "join_post_id", nullable = false)
    private JoinPost joinPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    @Column(nullable = false)
    private Integer rating; // 1~5

    @Column(nullable = false, name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
