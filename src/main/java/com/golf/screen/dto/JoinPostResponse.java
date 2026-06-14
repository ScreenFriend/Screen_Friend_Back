package com.golf.screen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.golf.screen.entity.JoinPost;
import com.golf.screen.entity.JoinStatus;
import com.golf.screen.entity.PaymentType;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class JoinPostResponse {
    private Long id;
    private String title;
    private String golfCenterName;
    private LocalDateTime playDateTime;
    private int maxPlayers;
    private int currentPlayers;
    private String description;
    private String address;
    private String dong;

    @JsonProperty("isReserved")
    private boolean isReserved;

    private Long creatorId;
    private String creatorNickname;
    private String creatorProfileImageUrl;
    private Double creatorMannerTemperature;
    private JoinStatus status;
    private String myApplicationStatus; // PENDING, ACCEPTED, REJECTED 등
    private PaymentType paymentType;
    private String phone;

    public static JoinPostResponse from(JoinPost joinPost) {
        return from(joinPost, null);
    }

    public static JoinPostResponse from(JoinPost joinPost, String myApplicationStatus) {
        return JoinPostResponse.builder()
                .id(joinPost.getId())
                .title(joinPost.getTitle())
                .golfCenterName(joinPost.getGolfCenterName())
                .playDateTime(joinPost.getPlayDateTime())
                .maxPlayers(joinPost.getMaxPlayers())
                .currentPlayers(joinPost.getCurrentPlayers())
                .description(joinPost.getDescription())
                .address(joinPost.getAddress())
                .dong(joinPost.getDong())
                .isReserved(joinPost.isReserved())
                .creatorId(joinPost.getCreator() != null ? joinPost.getCreator().getId() : null)
                .creatorNickname(joinPost.getCreator() != null ? joinPost.getCreator().getNickname() : null)
                .creatorProfileImageUrl(joinPost.getCreator() != null ? joinPost.getCreator().getProfileImageUrl() : null)
                .creatorMannerTemperature(joinPost.getCreator() != null ? joinPost.getCreator().getMannerTemperature() : null)
                .status(joinPost.getStatus())
                .myApplicationStatus(myApplicationStatus)
                .paymentType(joinPost.getPaymentType())
                .phone(joinPost.getGolfCenterPhone())
                .build();
    }
}
