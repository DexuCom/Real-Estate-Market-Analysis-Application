package com.rema.web_api.JWT;

import com.rema.web_api.enums.Role;
import com.rema.web_api.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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


    public String generateToken(User user)
    {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + this.jwtExpirationTime))
                .signWith(getRealKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }


    private Claims getClaimsFromToken(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(getRealKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

    }

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

    public Role getUserRoleFromToken(String token)
    {
        return extractClaim(token, claims ->  Role.valueOf(claims.get("role", String.class)));
    }

    public boolean isTokenExpired(String token)
    {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Key getRealKey()
    {
        byte[] bytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(bytes);
    }


}
