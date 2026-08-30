package com.dufeng.security;

import com.dufeng.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("dWZlbmctZWNvbW1lcmNlLWp3dC1zZWNyZXQta2V5LTIwMjYtMDEyMzQ1Njc4OQ==");
        properties.setExpireSeconds(3600);
        properties.setPrefix("Bearer ");
        jwtUtil = new JwtUtil(properties);
    }

    @Test
    void createAndParseTokenShouldRoundTrip() {
        String token = jwtUtil.createToken(42L, "alice", List.of("USER"));
        Claims claims = jwtUtil.parseToken(token);
        assertNotNull(claims);
        assertEquals("alice", claims.getSubject());
        assertEquals(42L, claims.get("uid", Number.class).longValue());
    }
}
