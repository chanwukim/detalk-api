package net.detalk.api.support.security;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OAuthUser implements OAuth2User {
    private final Long id;
    private final String accessToken;
    private final String refreshToken;
    private final String username;
    private final List<GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    @Builder
    public OAuthUser(Long id, String accessToken, String refreshToken, String username, List<GrantedAuthority> authorities, Map<String, Object> attributes) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
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
