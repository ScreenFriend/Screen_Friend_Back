package com.golf.screen.dto;

import com.golf.screen.entity.Gender;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignUpRequest {
    private String email;
    private String password;
    private String passwordConfirm;
    private String nickname;
    private Gender gender;
    private String phoneNumber;
    private String identityVerificationId; // 포트원 V2 본인인증 ID
}
