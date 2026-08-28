package com.campus.market.security;

import com.campus.market.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long hours;
    public JwtService(@Value("${app.jwt-secret}") String secret, @Value("${app.jwt-expiration-hours}") long hours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.hours = hours;
    }
    public String create(User user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(String.valueOf(user.id)).claim("role",user.role.name())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(hours*3600))).signWith(key).compact();
    }
    public Claims parse(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
}
