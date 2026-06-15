package com.golf.screen.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    JOIN_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "JOIN-001", "해당 조인 모집글을 찾을 수 없습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-001", "올바르지 않은 입력값입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "USER-001", "이미 존재하는 이메일입니다."),
    PASSWORD_CONFIRM_NOT_MATCH(HttpStatus.BAD_REQUEST, "USER-002", "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    PORTONE_AUTH_FAILED(HttpStatus.BAD_REQUEST, "AUTH-001", "포트원 본인인증 검증에 실패했습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-003", "존재하지 않는 사용자입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-001", "파일 업로드에 실패했습니다."),
    INVALID_JOIN_STATUS(HttpStatus.BAD_REQUEST, "JOIN-002", "이미 완료되거나 취소된 조인 모집글입니다."),
    JOIN_POST_FULL(HttpStatus.BAD_REQUEST, "JOIN-003", "모집 정원이 초과되었습니다."),
    INVALID_LEAVE_REQUEST(HttpStatus.BAD_REQUEST, "JOIN-004", "참가 신청 취소가 불가능한 상태입니다."),
    ALREADY_REVIEWED(HttpStatus.BAD_REQUEST, "REVIEW-001", "이미 평가를 완료한 대상입니다."),
    INVALID_REVIEWER(HttpStatus.BAD_REQUEST, "REVIEW-002", "해당 조인에 참여하지 않아 리뷰 권한이 없습니다."),
    INVALID_REVIEW_TARGET(HttpStatus.BAD_REQUEST, "REVIEW-003", "리뷰 대상이 조인 참가자가 아닙니다."),
    PLAY_DATE_NOT_PASSED(HttpStatus.BAD_REQUEST, "REVIEW-004", "경기 예약 시간으로부터 1시간이 경과해야 평가할 수 있습니다."),
    INVALID_PLAY_DATE(HttpStatus.BAD_REQUEST, "JOIN-005", "조인 예약 시간은 현재 시간으로부터 최소 10분 이후로만 개설할 수 있습니다."),
    SOCIAL_USER_CANNOT_RESET(HttpStatus.BAD_REQUEST, "USER-004", "소셜 가입 계정은 비밀번호 재설정이 불가능합니다. 소셜 로그인을 이용해 주세요."),
    USER_INFO_NOT_MATCH(HttpStatus.BAD_REQUEST, "USER-005", "입력하신 정보와 일치하는 회원이 없습니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "USER-006", "비밀번호는 영문, 숫자, 특수문자를 혼합하여 8자 이상이어야 합니다."),
    PASSWORD_CONTAINS_SEQUENCE(HttpStatus.BAD_REQUEST, "USER-007", "비밀번호에 연속된 문자 또는 숫자, 키보드 배열 패턴이 포함되어 있어 사용할 수 없습니다."),
    PHONE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "USER-008", "이미 해당 연락처로 가입된 계정이 존재합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
