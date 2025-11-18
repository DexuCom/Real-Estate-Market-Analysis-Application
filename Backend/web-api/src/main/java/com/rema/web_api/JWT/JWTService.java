package com.rema.web_api.JWT;

import com.rema.web_api.enums.Role;
import com.rema.web_api.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTService {
    @Value("${jwt.key}")
    private String key;
    @Value("${jwt.expiration}")
    private long jwtExpirationTime;

    @Transactional
    public String generateToken(User user)
    {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("username", user.getUsername());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + this.jwtExpirationTime))
                .signWith(getRealKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    @Transactional
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    @Transactional
    private Claims getClaimsFromToken(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(getRealKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

    }

    @Transactional
    public boolean isTokenValid(String token, User user)
    {
        if(isTokenExpired(token))
        {
            return false;
        }
        String tokenUsername = extractClaim(token, Claims::getSubject);

        if(tokenUsername.equals(user.getUsername()) == false)
        {
            return false;
        }

        return true;

    }

    @Transactional
    public Role getUserRoleFromToken(String token)
    {
        return extractClaim(token, claims ->  Role.valueOf(claims.get("role", String.class)));
    }

    @Transactional
    public boolean isTokenExpired(String token)
    {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    @Transactional
    private Key getRealKey()
    {
        byte[] bytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(bytes);
    }


}
