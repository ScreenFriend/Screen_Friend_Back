package com.golf.screen.controller;

import com.golf.screen.dto.PortOneVerificationRequest;
import com.golf.screen.service.PortOneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/portone")
@RequiredArgsConstructor
public class PortOneController {

    private final PortOneService portOneService;

    /**
     * 포트원 본인인증 단독 검증 API
     * 프론트엔드에서 인증 완료 후 imp_uid를 전송하면, 본인인증 성공한 사용자의 이름/전화번호/성별 정보를 검증하여 반환합니다.
     */
    @PostMapping("/verify")
    public ResponseEntity<PortOneService.CertificationInfo> verifyCertification(
            @RequestBody PortOneVerificationRequest request) {
        PortOneService.CertificationInfo info = portOneService.verifyCertification(request.getIdentityVerificationId());
        return ResponseEntity.ok(info);
    }
}
