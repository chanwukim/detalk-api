package net.detalk.api.support.security.oauth;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CustomOAuth2User implements OAuth2User {
    private final Long id;
    private final String accessToken;
    private final String refreshToken;
    private final String username;
    private final boolean isNew;
    private final List<GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    @Builder
    public CustomOAuth2User(Long id, String accessToken, String refreshToken, String username,
        boolean isNew, List<GrantedAuthority> authorities, Map<String, Object> attributes) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.isNew = isNew;
        this.authorities = authorities;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return username;
    }

}
