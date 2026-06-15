package com.golf.screen.controller;

import com.golf.screen.dto.UserResponse;
import com.golf.screen.dto.UserStatsResponse;
import com.golf.screen.dto.UserProfileResponse;
import com.golf.screen.dto.UserSignUpRequest;
import com.golf.screen.dto.UserUpdateRequest;
import com.golf.screen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원 가입
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(@RequestBody UserSignUpRequest request) {
        UserResponse response = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 회원 정보 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        UserResponse response = userService.getUser(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 타인 프로필 정보 조회 (프로필 카드용)
     */
    @GetMapping("/{id}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long id) {
        UserProfileResponse response = userService.getUserProfile(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인 세션 사용자 조회
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse response = userService.getUserByEmail(principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인한 사용자의 대시보드 스탯 정보 조회
     */
    @GetMapping("/me/stats")
    public ResponseEntity<UserStatsResponse> getMyStats(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserStatsResponse response = userService.getMyStats(principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 정보 및 프로필 사진 수정
     * MultipartForm 데이터 수신을 위해 @ModelAttribute를 사용합니다.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @ModelAttribute UserUpdateRequest request) {
        UserResponse response = userService.updateProfile(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> withdraw(@PathVariable Long id) {
        userService.withdraw(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 삭제 동네 추가
     */
    @PostMapping("/me/removed-dongs")
    public ResponseEntity<UserResponse> addRemovedDong(
            @RequestParam String dongName,
            java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse response = userService.addRemovedDong(principal.getName(), dongName);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 삭제 동네 복구
     */
    @DeleteMapping("/me/removed-dongs")
    public ResponseEntity<UserResponse> removeRemovedDong(
            @RequestParam String dongName,
            java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse response = userService.removeRemovedDong(principal.getName(), dongName);
        return ResponseEntity.ok(response);
    }

    /**
     * 삭제 동네 전체 초기화 (복구)
     */
    @DeleteMapping("/me/removed-dongs/clear")
    public ResponseEntity<UserResponse> clearRemovedDongs(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse response = userService.clearRemovedDongs(principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * 임시 비밀번호 발급 및 SMS 문자 전송
     */
    @PostMapping("/find-password")
    public ResponseEntity<Void> findPassword(@RequestBody com.golf.screen.dto.FindPasswordRequest request) {
        userService.findAndResetPassword(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 이름과 연락처로 가입 이메일 찾기 (아이디 찾기)
     */
    @PostMapping("/find-id")
    public ResponseEntity<com.golf.screen.dto.FindIdResponse> findId(@RequestBody com.golf.screen.dto.FindIdRequest request) {
        com.golf.screen.dto.FindIdResponse response = userService.findEmailByNameAndPhone(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 소셜 가입 사용자의 최초 본인인증 정보 연동 및 중복 검증
     */
    @PostMapping("/{id}/social-verify")
    public ResponseEntity<UserResponse> verifySocialUser(
            @PathVariable Long id,
            @RequestParam String identityVerificationId) {
        UserResponse response = userService.verifySocialUser(id, identityVerificationId);
        return ResponseEntity.ok(response);
    }
}
