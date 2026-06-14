package com.golf.screen.service;

import com.golf.screen.entity.Gender;
import com.golf.screen.error.CustomException;
import com.golf.screen.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortOneService {

    @Value("${portone.api.secret}")
    private String apiSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @Getter
    public static class CertificationInfo {
        private final String name;
        private final String phone;
        private final Gender gender;

        public CertificationInfo(String name, String phone, Gender gender) {
            this.name = name;
            this.phone = phone;
            this.gender = gender;
        }
    }

    /**
     * 포트원 V2 API를 호출하여 identityVerificationId에 해당하는 본인인증을 확인 및 조회합니다.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CertificationInfo verifyCertification(String identityVerificationId) {
        // API Secret이 설정되지 않았거나 기본값인 경우, 로컬 개발을 위한 Mock 데이터를 반환합니다.
        if (apiSecret == null || apiSecret.startsWith("YOUR_")) {
            System.out.println("[PortOne V2 MOCK Mode] API Secret이 기본값이므로 가상 인증 처리를 수행합니다.");
            return new CertificationInfo("홍길동", "01012345678", Gender.MALE);
        }

        try {
            // 포트원 V2 본인인증 완료 확정(Confirm) API
            String url = "https://api.portone.io/identity-verifications/" + identityVerificationId + "/confirm";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // V2 인증 방식: "PortOne <API_SECRET>"
            headers.set("Authorization", "PortOne " + apiSecret);

            // POST /identity-verifications/{id}/confirm 요청은 바디가 필요 없음
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(new HashMap<>(), headers);

            Map<String, Object> verificationMap = null;
            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
                if (response.getBody() != null) {
                    verificationMap = (Map<String, Object>) response.getBody().get("identityVerification");
                }
            } catch (org.springframework.web.client.HttpStatusCodeException e) {
                // 이미 인증 완료된 경우 (409 Conflict)
                if (e.getStatusCode().value() == 409
                        && e.getResponseBodyAsString().contains("IDENTITY_VERIFICATION_ALREADY_VERIFIED")) {
                    System.out.println("[PortOne V2] 이미 완료된 본인인증건입니다. 단건 조회를 시도합니다. ID: " + identityVerificationId);
                    String getUrl = "https://api.portone.io/identity-verifications/" + identityVerificationId;
                    HttpEntity<Void> getEntity = new HttpEntity<>(headers);
                    ResponseEntity<Map> getResponse = restTemplate.exchange(getUrl,
                            org.springframework.http.HttpMethod.GET, getEntity, Map.class);

                    System.out.println("[PortOne V2] 단건 조회 응답 바디: " + getResponse.getBody());

                    if (getResponse.getBody() != null) {
                        if (getResponse.getBody().containsKey("identityVerification")) {
                            verificationMap = (Map<String, Object>) getResponse.getBody().get("identityVerification");
                        } else {
                            verificationMap = (Map<String, Object>) getResponse.getBody();
                        }
                        System.out.println("[PortOne V2] 파싱 완료된 verificationMap: " + verificationMap);
                    }
                } else {
                    throw e;
                }
            }

            if (verificationMap == null) {
                throw new CustomException(ErrorCode.PORTONE_AUTH_FAILED);
            }

            // 본인인증 완료 상태 검증
            String status = (String) verificationMap.get("status");
            if (status == null || !status.equalsIgnoreCase("VERIFIED")) {
                throw new CustomException(ErrorCode.PORTONE_AUTH_FAILED);
            }

            // 데이터 추출 (V2 명세: verifiedCustomer 맵 객체 하위에 실 데이터 수록됨)
            Map<String, Object> customerMap = (Map<String, Object>) verificationMap.get("verifiedCustomer");
            if (customerMap == null) {
                customerMap = verificationMap; // 폴백
            }

            String name = (String) customerMap.get("name");
            String phone = (String) customerMap.get("phoneNumber"); // V2에서는 phoneNumber
            String genderStr = (String) customerMap.get("gender"); // "MALE" 또는 "FEMALE"

            Gender gender = Gender.MALE;
            if (genderStr != null && genderStr.equalsIgnoreCase("FEMALE")) {
                gender = Gender.FEMALE;
            }

            return new CertificationInfo(name, phone, gender);

        } catch (Exception e) {
            System.err.println("포트원 V2 인증 검증 에러: " + e.getMessage());
            if (e instanceof org.springframework.web.client.HttpStatusCodeException) {
                System.err.println("상세 바디: "
                        + ((org.springframework.web.client.HttpStatusCodeException) e).getResponseBodyAsString());
            }
            throw new CustomException(ErrorCode.PORTONE_AUTH_FAILED);
        }
    }
}
