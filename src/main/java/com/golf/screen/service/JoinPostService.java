package com.golf.screen.service;

import com.golf.screen.dto.JoinPostRequest;
import com.golf.screen.dto.JoinPostResponse;
import com.golf.screen.entity.JoinPost;
import com.golf.screen.entity.JoinStatus;
import com.golf.screen.error.CustomException;
import com.golf.screen.error.ErrorCode;
import com.golf.screen.repository.JoinPostRepository;
import com.golf.screen.repository.UserRepository;
import com.golf.screen.repository.JoinApplicationRepository;
import com.golf.screen.entity.User;
import com.golf.screen.entity.JoinApplication;
import com.golf.screen.entity.ApplicationStatus;
import com.golf.screen.dto.JoinApplicationResponse;
import com.golf.screen.repository.ChatMessageRepository;
import com.golf.screen.config.ChatWebSocketHandler;
import com.golf.screen.entity.ChatMessage;
import com.golf.screen.entity.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoinPostService {

    private final JoinPostRepository joinPostRepository;
    private final UserRepository userRepository;
    private final JoinApplicationRepository joinApplicationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatWebSocketHandler chatWebSocketHandler;

    @Transactional
    public JoinPostResponse createJoinPost(JoinPostRequest request, String email) {
        if (request.getPlayDateTime() == null || request.getPlayDateTime().isBefore(java.time.LocalDateTime.now().plusMinutes(10))) {
            throw new CustomException(ErrorCode.INVALID_PLAY_DATE);
        }
        String dong = extractDong(request.getAddress());
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        JoinPost joinPost = JoinPost.builder()
                .title(request.getTitle())
                .golfCenterName(request.getGolfCenterName())
                .playDateTime(request.getPlayDateTime())
                .maxPlayers(request.getMaxPlayers())
                .currentPlayers(request.getCurrentPlayers())
                .description(request.getDescription())
                .address(request.getAddress())
                .dong(dong)
                .isReserved(request.isReserved())
                .creator(creator)
                .status(JoinStatus.RECRUITING)
                .paymentType(request.getPaymentType() != null ? request.getPaymentType() : PaymentType.DUTCH_PAY)
                .golfCenterPhone(request.getPhone())
                .build();

        JoinPost savedPost = joinPostRepository.save(joinPost);
        return JoinPostResponse.from(savedPost);
    }

    public List<JoinPostResponse> getAllJoinPosts(String dong) {
        List<JoinPost> posts;
        java.time.LocalDateTime limitTime = java.time.LocalDateTime.now().plusMinutes(10);
        if (dong != null && !dong.trim().isEmpty() && !"전체".equals(dong)) {
            posts = joinPostRepository.findByDongAndPlayDateTimeAfterAndStatus(dong, limitTime, JoinStatus.RECRUITING);
        } else {
            posts = joinPostRepository.findByPlayDateTimeAfterAndStatus(limitTime, JoinStatus.RECRUITING);
        }
        return posts.stream()
                .map(JoinPostResponse::from)
                .collect(Collectors.toList());
    }

    private String extractDong(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "기타";
        }
        String[] parts = address.split(" ");
        for (String part : parts) {
            if (part.endsWith("동") || part.endsWith("읍") || part.endsWith("면")) {
                return part.replaceAll("\\d+", "");
            }
        }
        return "기타";
    }

    public JoinPostResponse getJoinPost(Long id) {
        JoinPost joinPost = joinPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_POST_NOT_FOUND));
        return JoinPostResponse.from(joinPost);
    }

    @Transactional
    public JoinPostResponse updateStatus(Long id, JoinStatus status) {
        JoinPost joinPost = joinPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_POST_NOT_FOUND));
        joinPost.setStatus(status);
        return JoinPostResponse.from(joinPost);
    }

    @Transactional
    public JoinPostResponse joinPost(Long id) {
        JoinPost joinPost = joinPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_POST_NOT_FOUND));

        if (joinPost.getStatus() == JoinStatus.COMPLETED || joinPost.getStatus() == JoinStatus.CANCELLED) {
            throw new CustomException(ErrorCode.INVALID_JOIN_STATUS);
        }

        if (joinPost.getCurrentPlayers() >= joinPost.getMaxPlayers()) {
            throw new CustomException(ErrorCode.JOIN_POST_FULL);
        }

        joinPost.setCurrentPlayers(joinPost.getCurrentPlayers() + 1);

        if (joinPost.getCurrentPlayers() == joinPost.getMaxPlayers()) {
            joinPost.setStatus(JoinStatus.COMPLETED);
        }

        return JoinPostResponse.from(joinPost);
    }

    @Transactional
    public JoinPostResponse leavePost(Long id, String email) {
        JoinPost joinPost = joinPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_POST_NOT_FOUND));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 사용자의 참가 신청 내역 조회
        JoinApplication application = joinApplicationRepository.findByJoinPostIdAndApplicantId(id, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LEAVE_REQUEST));

        // 이미 거절된 상태라면 취소 불가
        if (application.getStatus() == ApplicationStatus.REJECTED) {
            throw new CustomException(ErrorCode.INVALID_LEAVE_REQUEST);
        }

        // 수락 상태였던 참가자가 취소하는 경우 인원 수 차감 및 모집 상태 복원
        boolean wasAccepted = application.getStatus() == ApplicationStatus.ACCEPTED;
        if (wasAccepted) {
            if (joinPost.getCurrentPlayers() <= 1) {
                throw new CustomException(ErrorCode.INVALID_LEAVE_REQUEST);
            }
            joinPost.setCurrentPlayers(joinPost.getCurrentPlayers() - 1);
            if (joinPost.getStatus() == JoinStatus.COMPLETED && joinPost.getCurrentPlayers() < joinPost.getMaxPlayers()) {
                joinPost.setStatus(JoinStatus.RECRUITING);
            }
        }

        // 신청 이력 데이터베이스에서 삭제
        joinApplicationRepository.delete(application);

        // 수락 상태인 유저가 탈퇴(취소)한 경우에만 퇴장 메시지 생성 및 전송
        if (wasAccepted) {
            ChatMessage systemMsg = ChatMessage.builder()
                    .joinPostId(joinPost.getId())
                    .senderId(0L)
                    .senderNickname("시스템")
                    .content(user.getNickname() + "님이 조인에서 나갔습니다.")
                    .sendTime(java.time.LocalDateTime.now())
                    .build();
            chatMessageRepository.save(systemMsg);
            chatWebSocketHandler.broadcastSystemMessage(joinPost.getId(), systemMsg);
        }

        return JoinPostResponse.from(joinPost);
    }

    @Transactional
    public void applyJoin(Long postId, String email) {
        JoinPost joinPost = joinPostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_POST_NOT_FOUND));

        User applicant = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (joinPost.getCreator() != null && joinPost.getCreator().getId().equals(applicant.getId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (joinPost.getStatus() == JoinStatus.COMPLETED || joinPost.getStatus() == JoinStatus.CANCELLED) {
            throw new CustomException(ErrorCode.INVALID_JOIN_STATUS);
        }

        joinApplicationRepository.findByJoinPostIdAndApplicantId(postId, applicant.getId())
                .ifPresent(app -> {
                    if (app.getStatus() == ApplicationStatus.PENDING || app.getStatus() == ApplicationStatus.ACCEPTED) {
                        throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
                    }
                });

        JoinApplication application = JoinApplication.builder()
                .joinPost(joinPost)
                .applicant(applicant)
                .status(ApplicationStatus.PENDING)
                .build();

        joinApplicationRepository.save(application);
    }

    public List<JoinApplicationResponse> getApplications(Long postId, String email) {
        JoinPost joinPost = joinPostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_POST_NOT_FOUND));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (joinPost.getCreator() == null || !joinPost.getCreator().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return joinApplicationRepository.findByJoinPostId(postId).stream()
                .map(JoinApplicationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public JoinPostResponse acceptApplication(Long applicationId, String email) {
        JoinApplication application = joinApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_POST_NOT_FOUND));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        JoinPost joinPost = application.getJoinPost();

        if (joinPost.getCreator() == null || !joinPost.getCreator().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_JOIN_STATUS);
        }

        if (joinPost.getCurrentPlayers() >= joinPost.getMaxPlayers()) {
            throw new CustomException(ErrorCode.JOIN_POST_FULL);
        }

        application.setStatus(ApplicationStatus.ACCEPTED);
        joinPost.setCurrentPlayers(joinPost.getCurrentPlayers() + 1);

        if (joinPost.getCurrentPlayers() == joinPost.getMaxPlayers()) {
            joinPost.setStatus(JoinStatus.COMPLETED);
        }

        // 시스템 참가 알림 메시지 생성 및 저장
        ChatMessage systemMsg = ChatMessage.builder()
                .joinPostId(joinPost.getId())
                .senderId(0L)
                .senderNickname("시스템")
                .content(application.getApplicant().getNickname() + "님이 조인에 참가했습니다.")
                .sendTime(java.time.LocalDateTime.now())
                .build();
        chatMessageRepository.save(systemMsg);

        // 실시간 브로드캐스트 전송
        chatWebSocketHandler.broadcastSystemMessage(joinPost.getId(), systemMsg);

        return JoinPostResponse.from(joinPost);
    }

    @Transactional
    public void rejectApplication(Long applicationId, String email) {
        JoinApplication application = joinApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_POST_NOT_FOUND));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        JoinPost joinPost = application.getJoinPost();

        if (joinPost.getCreator() == null || !joinPost.getCreator().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_JOIN_STATUS);
        }

        application.setStatus(ApplicationStatus.REJECTED);
    }

    public JoinApplicationResponse getMyApplication(Long postId, String email) {
        User applicant = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return joinApplicationRepository.findByJoinPostIdAndApplicantId(postId, applicant.getId())
                .map(JoinApplicationResponse::from)
                .orElse(null);
    }

    public List<JoinPostResponse> getMyCreatedJoins(String email) {
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        java.time.LocalDateTime limitTime = java.time.LocalDateTime.now().minusHours(1);
        return joinPostRepository.findByCreatorIdAndPlayDateTimeAfterOrderByPlayDateTimeDesc(creator.getId(), limitTime).stream()
                .map(JoinPostResponse::from)
                .collect(Collectors.toList());
    }

    public List<JoinPostResponse> getMyAppliedJoins(String email) {
        User applicant = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        java.time.LocalDateTime limitTime = java.time.LocalDateTime.now().minusHours(1);
        List<ApplicationStatus> statuses = List.of(ApplicationStatus.ACCEPTED, ApplicationStatus.PENDING);
        return joinApplicationRepository.findActiveAppliedJoins(applicant.getId(), statuses, limitTime).stream()
                .map(app -> JoinPostResponse.from(app.getJoinPost(), app.getStatus().name()))
                .collect(Collectors.toList());
    }

    public List<JoinPostResponse> getMyCompletedJoins(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        java.time.LocalDateTime limitTime = java.time.LocalDateTime.now().minusHours(1);
        return joinPostRepository.findMyCompletedJoins(user.getId(), ApplicationStatus.ACCEPTED, limitTime).stream()
                .map(JoinPostResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteJoinPost(Long id, String email) {
        JoinPost joinPost = joinPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_POST_NOT_FOUND));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 개설자 본인인지 확인
        if (joinPost.getCreator() == null || !joinPost.getCreator().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 2. 수락된 조인 참가 유저가 있는지 확인 (개설자 제외)
        boolean hasAcceptedApplicants = joinApplicationRepository.existsByJoinPostIdAndStatus(id, ApplicationStatus.ACCEPTED);
        if (hasAcceptedApplicants) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 3. 연관된 신청서들 일괄 삭제
        joinApplicationRepository.deleteByJoinPostId(id);

        // 4. 조인글 삭제
        joinPostRepository.delete(joinPost);
    }

    @Transactional
    public JoinPostResponse updateReserved(Long id, boolean isReserved, String email) {
        JoinPost joinPost = joinPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JOIN_POST_NOT_FOUND));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 개설자 본인 확인
        if (joinPost.getCreator() == null || !joinPost.getCreator().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        joinPost.setReserved(isReserved);
        return JoinPostResponse.from(joinPost);
    }
}
