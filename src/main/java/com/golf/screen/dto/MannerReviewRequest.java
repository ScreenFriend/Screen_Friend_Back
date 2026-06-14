package com.golf.screen.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MannerReviewRequest {
    private Long joinPostId;
    private Long targetUserId;
    private Integer rating;
}
