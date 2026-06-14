package com.golf.screen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawn_user_stats")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WithdrawnUserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 전화번호 단방향 해시값을 고유 식별자로 사용 (Unique)
    @Column(nullable = false, unique = true, name = "phone_number_hash")
    private String phoneNumberHash;

    @Column(nullable = false, name = "manner_temperature")
    private Double mannerTemperature;

    @Column(nullable = false, name = "withdrawn_at")
    private LocalDateTime withdrawnAt;
}
