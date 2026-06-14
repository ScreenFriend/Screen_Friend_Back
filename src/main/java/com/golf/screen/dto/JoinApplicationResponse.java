package com.golf.screen.dto;

import com.golf.screen.entity.ApplicationStatus;
import com.golf.screen.entity.JoinApplication;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class JoinApplicationResponse {
    private Long id;
    private Long applicantId;
    private String applicantNickname;
    private String applicantGender;
    private ApplicationStatus status;
    private String applicantProfileImageUrl;
    private Double applicantMannerTemperature;

    public static JoinApplicationResponse from(JoinApplication application) {
        return JoinApplicationResponse.builder()
                .id(application.getId())
                .applicantId(application.getApplicant().getId())
                .applicantNickname(application.getApplicant().getNickname())
                .applicantGender(application.getApplicant().getGender().name())
                .status(application.getStatus())
                .applicantProfileImageUrl(application.getApplicant().getProfileImageUrl())
                .applicantMannerTemperature(application.getApplicant().getMannerTemperature())
                .build();
    }
}
