package com.golf.screen.service;

import com.golf.screen.dto.MannerReviewRequest;
import com.golf.screen.dto.ParticipantResponse;
import com.golf.screen.entity.*;
import com.golf.screen.error.CustomException;
import com.golf.screen.error.ErrorCode;
import com.golf.screen.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MannerReviewService {

    private final MannerReviewRepository mannerReviewRepository;
    private final JoinPostRepository joinPostRepository;
    private final UserRepository userRepository;
    private final JoinApplicationRepository joinApplicationRepository;

    public List<ParticipantResponse> getParticipantsToReview(Long joinPostId, String email) {
        JoinPost joinPost = joinPostRepository.findById(joinPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_POST_NOT_FOUND));

        User reviewer = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 해당 조인의 모든 ACCEPTED 참가자 및 개설자 수집
        List<User> participants = new ArrayList<>();
        if (joinPost.getCreator() != null) {
            participants.add(joinPost.getCreator());
        }

        List<JoinApplication> applications = joinApplicationRepository.findByJoinPostId(joinPostId);
        List<User> acceptedApplicants = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.ACCEPTED)
                .map(JoinApplication::getApplicant)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        participants.addAll(acceptedApplicants);

        // 리뷰어가 참가자인지 검증
        boolean isReviewerParticipant = participants.stream()
                .anyMatch(u -> u.getId().equals(reviewer.getId()));
        if (!isReviewerParticipant) {
            throw new CustomException(ErrorCode.INVALID_REVIEWER);
        }

        // 리뷰어 본인을 리스트에서 제외
        return participants.stream()
                .filter(u -> !u.getId().equals(reviewer.getId()))
                .map(u -> {
                    boolean isReviewed = mannerReviewRepository.existsByJoinPostIdAndReviewerIdAndTargetUserId(
                            joinPostId, reviewer.getId(), u.getId()
                    );
                    return ParticipantResponse.builder()
                            .id(u.getId())
                            .nickname(u.getNickname())
                            .profileImageUrl(u.getProfileImageUrl())
                            .mannerTemperature(u.getMannerTemperature())
                            .isReviewed(isReviewed)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void createMannerReview(MannerReviewRequest request, String email) {
        JoinPost joinPost = joinPostRepository.findById(request.getJoinPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_POST_NOT_FOUND));

        User reviewer = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 경기 예약 시간 1시간 경과 여부 검증
        if (joinPost.getPlayDateTime().plusHours(1).isAfter(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.PLAY_DATE_NOT_PASSED);
        }

        // 2. 평점 범위 검증
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 3. 본인 평가 검증
        if (reviewer.getId().equals(targetUser.getId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 4. 참가자 리스트 수집 및 검증
        List<User> participants = new ArrayList<>();
        if (joinPost.getCreator() != null) {
            participants.add(joinPost.getCreator());
        }
        List<JoinApplication> applications = joinApplicationRepository.findByJoinPostId(request.getJoinPostId());
        List<User> acceptedApplicants = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.ACCEPTED)
                .map(JoinApplication::getApplicant)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        participants.addAll(acceptedApplicants);

        boolean isReviewerParticipant = participants.stream()
                .anyMatch(u -> u.getId().equals(reviewer.getId()));
        if (!isReviewerParticipant) {
            throw new CustomException(ErrorCode.INVALID_REVIEWER);
        }

        boolean isTargetParticipant = participants.stream()
                .anyMatch(u -> u.getId().equals(targetUser.getId()));
        if (!isTargetParticipant) {
            throw new CustomException(ErrorCode.INVALID_REVIEW_TARGET);
        }

        // 5. 중복 평가 검증
        if (mannerReviewRepository.existsByJoinPostIdAndReviewerIdAndTargetUserId(
                request.getJoinPostId(), reviewer.getId(), targetUser.getId()
        )) {
            throw new CustomException(ErrorCode.ALREADY_REVIEWED);
        }

        // 6. 리뷰 데이터 저장
        MannerReview review = MannerReview.builder()
                .joinPost(joinPost)
                .reviewer(reviewer)
                .targetUser(targetUser)
                .rating(request.getRating())
                .build();
        mannerReviewRepository.save(review);

        // 7. 매너 온도 반영 로직: (rating - 3) * 0.1
        double delta = (request.getRating() - 3) * 0.1;
        double currentTemp = targetUser.getMannerTemperature() != null ? targetUser.getMannerTemperature() : 36.5;
        double newTemp = Math.max(0.0, Math.min(99.9, currentTemp + delta));
        // 소수점 첫째자리까지 반올림
        newTemp = Math.round(newTemp * 10.0) / 10.0;

        targetUser.setMannerTemperature(newTemp);
        userRepository.save(targetUser);
    }
}
