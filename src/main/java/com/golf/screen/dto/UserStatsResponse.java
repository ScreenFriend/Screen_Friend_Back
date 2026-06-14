package com.golf.screen.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsResponse {
    private Integer pendingJoinsCount;
    private Integer averageScore;
    private Double mannerTemperature;
}
