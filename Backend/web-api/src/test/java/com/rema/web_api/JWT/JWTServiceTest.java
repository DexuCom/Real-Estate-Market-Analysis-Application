package com.rema.web_api.JWT;

import com.rema.web_api.enums.Role;
import com.rema.web_api.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @InjectMocks
    private JWTService jwtService;

    private User user;

    private final String BASE64_KEY = "ZmQwZjNlMjRiNmY2NzY2ODVmNTQ1ODlmZjQyZDI1MzA4ZjZjNDQ3ZTI4NDMzYjg1";

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(jwtService, "key", BASE64_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationTime", 3600000L); // 1h

        user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .role(Role.USER)
                .build();
    }

    private Key getRealKey() {
        return (Key) ReflectionTestUtils.invokeMethod(jwtService, "getRealKey");
    }

    @Test
    void generateToken_Success() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getRealKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(user.getId().toString(), claims.getSubject());
        assertEquals("USER", claims.get("role", String.class));
        assertEquals("testuser", claims.get("username", String.class));
    }

    @Test
    void extractClaim_Success() {
        String token = jwtService.generateToken(user);

        String subject = jwtService.extractClaim(token, Claims::getSubject);

        assertEquals(user.getId().toString(), subject);
    }

    @Test
    void getUserRoleFromToken_Success() {
        String token = jwtService.generateToken(user);

        Role role = jwtService.getUserRoleFromToken(token);

        assertEquals(Role.USER, role);
    }

    @Test
    void isTokenExpired_Failure() {
        String token = jwtService.generateToken(user);

        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_Success() {
        Key key = getRealKey();

        Claims claims = Jwts.claims();
        claims.setSubject(user.getId().toString());
        claims.setExpiration(new Date(System.currentTimeMillis() - 1000));
        claims.put("role", "USER");
        claims.put("username", "testuser");

        String expiredToken = Jwts.builder()
                .setClaims(claims)
                .signWith(key)
                .compact();

        boolean result = jwtService.isTokenExpired(expiredToken);

        assertTrue(result);
    }


    @Test
    void extractClaim_Failure() {
        assertThrows(Exception.class, () -> {
            jwtService.extractClaim("invalid.token.value", Claims::getSubject);
        });
    }
}
