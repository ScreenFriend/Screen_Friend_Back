package com.golf.screen.dto;

import com.golf.screen.entity.AuthProvider;
import com.golf.screen.entity.Gender;
import com.golf.screen.entity.User;
import lombok.*;
import java.util.List;
import java.util.Collections;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String nickname;
    private Gender gender;
    private String phoneNumber;
    private String profileImageUrl;
    private AuthProvider provider;
    private Double mannerTemperature;
    private Integer averageScore;
    private String bio;
    private List<String> removedDongs;

    public static UserResponse from(User user) {
        return from(user, Collections.emptyList());
    }

    public static UserResponse from(User user, List<String> removedDongs) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileImageUrl())
                .provider(user.getProvider())
                .mannerTemperature(user.getMannerTemperature())
                .averageScore(user.getAverageScore())
                .bio(user.getBio())
                .removedDongs(removedDongs)
                .build();
    }
}
