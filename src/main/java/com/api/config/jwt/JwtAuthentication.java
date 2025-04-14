package com.api.config.jwt;

import com.api.config.enums.Role;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Class JwtAuthentication
 *
 * A class representing the user's authentication details,
 * used to store information about the user after successful authentication.
 * Implements the {@link Authentication} interface for integration with Spring Security.
 */
@Getter
@Setter
public class JwtAuthentication implements Authentication {
    private boolean authenticated;
    private String email;
    private String fullName;
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role != null
                ? List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                : List.of();
    }

    @Override
    public Object getCredentials() { return null; }

    @Override
    public Object getDetails() { return null; }

    @Override
    public Object getPrincipal() { return email; }

    @Override
    public boolean isAuthenticated() { return authenticated; }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() { return fullName; }

}