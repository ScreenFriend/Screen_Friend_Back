package com.golf.screen.service;

import com.golf.screen.dto.UserSignUpRequest;
import com.golf.screen.dto.UserResponse;
import com.golf.screen.dto.UserUpdateRequest;
import com.golf.screen.dto.UserProfileResponse;
import com.golf.screen.entity.AuthProvider;
import com.golf.screen.entity.User;
import com.golf.screen.entity.RemovedDong;
import com.golf.screen.entity.WithdrawnUserStats;
import com.golf.screen.error.CustomException;
import com.golf.screen.error.ErrorCode;
import com.golf.screen.dto.UserStatsResponse;
import com.golf.screen.repository.JoinPostRepository;
import com.golf.screen.repository.JoinApplicationRepository;
import com.golf.screen.repository.UserRepository;
import com.golf.screen.repository.RemovedDongRepository;
import com.golf.screen.repository.WithdrawnUserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final JoinPostRepository joinPostRepository;
    private final JoinApplicationRepository joinApplicationRepository;
    private final RemovedDongRepository removedDongRepository;
    private final WithdrawnUserStatsRepository withdrawnUserStatsRepository;
    private final PortOneService portOneService;
    private final FileService fileService;
    private final PasswordEncoder passwordEncoder;

    private String hashPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(phoneNumber.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("전화번호 해싱 실패", e);
        }
    }

    /**
     * 회원 가입
     */
    @Transactional
    public UserResponse signUp(UserSignUpRequest request) {
        // 1. 비밀번호 확인 일치 체크
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }

        // 2. 포트원 실명/번호/성별 인증 대조
        PortOneService.CertificationInfo certInfo = portOneService.verifyCertification(request.getIdentityVerificationId());

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4. 탈퇴한 회원인지 체크 및 이전 매너 온도 가져오기
        double initialMannerTemperature = 36.5;
        String phoneHash = hashPhoneNumber(certInfo.getPhone());
        java.util.Optional<WithdrawnUserStats> withdrawnStats = withdrawnUserStatsRepository.findByPhoneNumberHash(phoneHash);
        if (withdrawnStats.isPresent()) {
            initialMannerTemperature = withdrawnStats.get().getMannerTemperature();
            // 재가입 완료 시 기존 탈퇴 기록은 파기
            withdrawnUserStatsRepository.delete(withdrawnStats.get());
        }

        // 5. 이메일 중복 체크 및 소셜 계정 통합 처리
        java.util.Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());
        User user;
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            // 만약 이미 일반(LOCAL) 계정으로 가입되어 있다면 중복 가입 에러
            if (existingUser.getProvider() == AuthProvider.LOCAL) {
                throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            
            // 소셜 가입만 되어 있는 상태라면, 일반 로그인 정보(비밀번호, 실제 전화번호, 성별)를 덧입혀 계정 통합 진행
            existingUser.setPassword(encodedPassword);
            existingUser.setNickname(request.getNickname());
            existingUser.setGender(certInfo.getGender());
            existingUser.setPhoneNumber(certInfo.getPhone());
            existingUser.setProvider(AuthProvider.LOCAL); // 일반 로그인도 되도록 provider를 LOCAL로 통합 변경
            existingUser.setMannerTemperature(initialMannerTemperature);
            
            user = userRepository.save(existingUser);
        } else {
            // 최초 신규 가입
            user = User.builder()
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .nickname(request.getNickname())
                    .gender(certInfo.getGender())
                    .phoneNumber(certInfo.getPhone())
                    .mannerTemperature(initialMannerTemperature)
                    .provider(AuthProvider.LOCAL)
                    .build();
            user = userRepository.save(user);
        }

        return UserResponse.from(user);
    }

    /**
     * 회원 조회
     */
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        List<String> removedDongs = removedDongRepository.findByUser(user).stream()
                .map(RemovedDong::getDongName)
                .collect(Collectors.toList());
        return UserResponse.from(user, removedDongs);
    }

    /**
     * 이메일로 회원 조회 (현재 로그인 세션용)
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        List<String> removedDongs = removedDongRepository.findByUser(user).stream()
                .map(RemovedDong::getDongName)
                .collect(Collectors.toList());
        return UserResponse.from(user, removedDongs);
    }

    /**
     * 프로필 및 회원 정보 수정
     */
    @Transactional
    public UserResponse updateProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 닉네임 수정
        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            user.setNickname(request.getNickname());
        }

        // 2. 비밀번호 수정 (입력했을 시에만)
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // 3. 프로필 이미지 수정 (로컬 업로드)
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            // 기존 이미지 삭제
            if (user.getProfileImageUrl() != null) {
                fileService.deleteFile(user.getProfileImageUrl());
            }
            // 새 이미지 업로드 및 저장
            String newImageUrl = fileService.uploadFile(request.getProfileImage());
            user.setProfileImageUrl(newImageUrl);
        }

        // 4. 평균 타수 수정
        user.setAverageScore(request.getAverageScore());

        // 5. 간단 소개 수정
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        List<String> removedDongs = removedDongRepository.findByUser(user).stream()
                .map(RemovedDong::getDongName)
                .collect(Collectors.toList());
        return UserResponse.from(user, removedDongs);
    }

    @Transactional
    public UserResponse addRemovedDong(String email, String dongName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (removedDongRepository.findByUserAndDongName(user, dongName).isEmpty()) {
            RemovedDong removedDong = RemovedDong.builder()
                    .user(user)
                    .dongName(dongName)
                    .build();
            removedDongRepository.save(removedDong);
        }

        List<String> removedDongs = removedDongRepository.findByUser(user).stream()
                .map(RemovedDong::getDongName)
                .collect(Collectors.toList());
        return UserResponse.from(user, removedDongs);
    }

    @Transactional
    public UserResponse removeRemovedDong(String email, String dongName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        removedDongRepository.deleteByUserAndDongName(user, dongName);

        List<String> removedDongs = removedDongRepository.findByUser(user).stream()
                .map(RemovedDong::getDongName)
                .collect(Collectors.toList());
        return UserResponse.from(user, removedDongs);
    }

    @Transactional
    public UserResponse clearRemovedDongs(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        removedDongRepository.deleteByUser(user);

        return UserResponse.from(user, java.util.Collections.emptyList());
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 탈퇴 방지 해시 로그 남기기 (전화번호 단방향 암호화 및 매너 온도 매핑)
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
            String phoneHash = hashPhoneNumber(user.getPhoneNumber());
            
            // 기존에 해당 번호에 대한 탈퇴 이력이 이미 존재한다면 최신 값으로 업데이트
            WithdrawnUserStats stats = withdrawnUserStatsRepository.findByPhoneNumberHash(phoneHash)
                    .orElseGet(() -> WithdrawnUserStats.builder().phoneNumberHash(phoneHash).build());
            
            stats.setMannerTemperature(user.getMannerTemperature());
            stats.setWithdrawnAt(LocalDateTime.now());
            withdrawnUserStatsRepository.save(stats);
        }

        // 2. 프로필 파일이 있으면 삭제
        if (user.getProfileImageUrl() != null) {
            fileService.deleteFile(user.getProfileImageUrl());
        }

        userRepository.delete(user);
    }

    /**
     * 내 스탯 정보 조회
     */
    public UserStatsResponse getMyStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime limitTime = LocalDateTime.now().minusHours(1);

        // 1. 대기 중인 신청 건수 조회 (PENDING) 중 예약 시간 1시간 경과 전인 건 필터링
        long pendingApplicationsCount = joinApplicationRepository.findByApplicantIdAndStatus(
                user.getId(), com.golf.screen.entity.ApplicationStatus.PENDING
        ).stream()
         .filter(app -> app.getJoinPost().getPlayDateTime().isAfter(limitTime))
         .count();

        // 2. 본인이 만든 모집 중인 조인 건수 조회 (RECRUITING) 중 예약 시간 1시간 경과 전인 건 필터링
        long recruitingJoinsCount = joinPostRepository.findByCreatorIdAndStatus(
                user.getId(), com.golf.screen.entity.JoinStatus.RECRUITING
        ).stream()
         .filter(post -> post.getPlayDateTime().isAfter(limitTime))
         .count();

        int totalPendingJoins = (int) (pendingApplicationsCount + recruitingJoinsCount);

        return UserStatsResponse.builder()
                .pendingJoinsCount(totalPendingJoins)
                .averageScore(user.getAverageScore())
                .mannerTemperature(user.getMannerTemperature())
                .build();
    }

    /**
     * 타인 프로필 정보 조회 (프로필 카드용)
     */
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserProfileResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .averageScore(user.getAverageScore())
                .mannerTemperature(user.getMannerTemperature())
                .bio(user.getBio())
                .build();
    }
}
