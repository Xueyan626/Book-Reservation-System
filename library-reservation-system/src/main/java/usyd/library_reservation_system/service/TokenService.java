package usyd.library_reservation_system.library_reservation_system.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class TokenService {

    @Value("${app.login.hmac-secret}")

    private String secret;
    private SecretKey key;

    @PostConstruct
    void init() {
        // Generate HS256 key (secret must be long enough)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String sign(Map<String, Object> claims, Instant expiresAt) {
        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public Map<String, Object> verify(String jwt) {
        // Returns Claims (Map) on success, throws exception on failure (expired/invalid signature)
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(jwt).getPayload();
    }
}
