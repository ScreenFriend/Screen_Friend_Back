package com.golf.screen.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantResponse {
    private Long id;
    private String nickname;
    private String profileImageUrl;
    private Double mannerTemperature;
    private boolean isReviewed;
}
