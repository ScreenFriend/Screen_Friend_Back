package com.golf.screen.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "removed_dongs",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "dong_name"})
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RemovedDong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "dong_name", nullable = false)
    private String dongName;
}
