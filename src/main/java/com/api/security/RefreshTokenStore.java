package com.api.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class RefreshTokenStore {

    private final ConcurrentMap<String, String> tokens = new ConcurrentHashMap<>();

    public boolean putIfAbsent(String email, String token) {
        return tokens.putIfAbsent(email, token) == null;
    }

    public boolean validateToken(String email, String token) {
        return token.equals(tokens.get(email));
    }

    public void updateToken(String email, String token) {
        tokens.put(email, token);
    }

    public void updateToken(String email, String oldToken, String newToken) {
        tokens.replace(email, oldToken, newToken);
    }

    public void invalidate(String email) {
        tokens.remove(email);
    }

    public boolean contains(String email) {
        return tokens.containsKey(email);
    }

    public String get(String email) {
        return tokens.get(email);
    }

    public void clear() {
        tokens.clear();
    }
}
