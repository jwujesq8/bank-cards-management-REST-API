package com.api.service.auth;

import com.api.entity.User;
import com.api.security.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;

    public String generateAccessToken(User user) {
        return jwtProvider.generateAccessToken(user);
    }

    public String generateRefreshToken(User user) {
        return jwtProvider.generateRefreshToken(user);
    }

    public boolean validateRefreshToken(String token) {
        return jwtProvider.validateRefreshToken(token);
    }

    public Claims getRefreshClaims(String token) {
        return jwtProvider.getRefreshClaims(token);
    }

}
