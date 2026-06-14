package com.golf.screen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.golf.screen.entity.PaymentType;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinPostRequest {
    private String title;
    private String golfCenterName;
    private LocalDateTime playDateTime;
    private int maxPlayers;
    private int currentPlayers;
    private String description;
    private String address;

    @JsonProperty("isReserved")
    private boolean isReserved;

    private PaymentType paymentType;
    private String phone;
}
