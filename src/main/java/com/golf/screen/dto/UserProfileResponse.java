package com.golf.screen.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private String nickname;
    private String profileImageUrl;
    private Integer averageScore;
    private Double mannerTemperature;
    private String bio;
}
