package com.golf.screen.service;

import com.golf.screen.dto.oauth.GoogleUserInfo;
import com.golf.screen.dto.oauth.KakaoUserInfo;
import com.golf.screen.dto.oauth.OAuth2UserInfo;
import com.golf.screen.entity.AuthProvider;
import com.golf.screen.entity.Gender;
import com.golf.screen.entity.User;
import com.golf.screen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo oAuth2UserInfo = null;
        AuthProvider provider = AuthProvider.LOCAL;

        if (registrationId.equalsIgnoreCase("google")) {
            oAuth2UserInfo = new GoogleUserInfo(attributes);
            provider = AuthProvider.GOOGLE;
        } else if (registrationId.equalsIgnoreCase("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo(attributes);
            provider = AuthProvider.KAKAO;
        }

        if (oAuth2UserInfo == null) {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인 제공처입니다.");
        }

        String email = oAuth2UserInfo.getEmail();
        if (email == null) {
            throw new OAuth2AuthenticationException("소셜 로그인 제공처로부터 이메일을 획득할 수 없습니다.");
        }

        // 유저 확인 및 가입/업데이트
        saveOrUpdateUser(oAuth2UserInfo, provider, email);

        // 시큐리티 세션용 OAuth2User 반환
        // Principal.getName()이 항상 사용자의 가입 이메일을 반환하도록 Attributes 최상위에 email을 직접 매핑
        java.util.Map<String, Object> customAttributes = new java.util.HashMap<>(oAuth2User.getAttributes());
        customAttributes.put("email", email);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                customAttributes,
                "email"
        );
    }

    private User saveOrUpdateUser(OAuth2UserInfo userInfo, AuthProvider provider, String email) {
        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    // 기존 회원 정보 동기화 (닉네임, 프로필 사진 변경 시 업데이트)
                    existingUser.setNickname(userInfo.getNickname());
                    if (userInfo.getProfileImageUrl() != null) {
                        existingUser.setProfileImageUrl(userInfo.getProfileImageUrl());
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    // 최초 소셜 가입자 생성
                    User newUser = User.builder()
                            .email(email)
                            .nickname(userInfo.getNickname())
                            .profileImageUrl(userInfo.getProfileImageUrl())
                            .provider(provider)
                            .providerId(userInfo.getId())
                            // 소셜 로그인 회원은 비밀번호가 없고, 전화번호와 성별은 임시 기본값 처리 (이후 본인인증을 통해 보완)
                            .password(null)
                            .phoneNumber("000-0000-0000")
                            .gender(Gender.MALE)
                            .build();
                    return userRepository.save(newUser);
                });
    }
}
