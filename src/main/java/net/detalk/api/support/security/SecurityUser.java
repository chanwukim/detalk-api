package net.detalk.api.support.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class SecurityUser implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUser(Long id, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = null;
        this.password = null;
        this.authorities = authorities;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
