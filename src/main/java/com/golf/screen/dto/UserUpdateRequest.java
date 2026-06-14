package com.golf.screen.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    private String password;
    private String nickname;
    private MultipartFile profileImage; // 수정 요청 시 업로드할 프로필 파일
    private Integer averageScore;
    private String bio;
}
