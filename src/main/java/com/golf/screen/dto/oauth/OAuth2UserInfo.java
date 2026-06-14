package com.golf.screen.dto.oauth;

import java.util.Map;

public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId(); // 소셜 고유 ID
    public abstract String getEmail(); // 소셜 이메일
    public abstract String getNickname(); // 소셜 닉네임
    public abstract String getProfileImageUrl(); // 소셜 프로필 이미지
}
