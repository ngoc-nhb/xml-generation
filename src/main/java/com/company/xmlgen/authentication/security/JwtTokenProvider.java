package com.company.xmlgen.authentication.security;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.authentication.service.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * JWT implementation of {@link TokenProvider}.
 */
@Component
public class JwtTokenProvider implements TokenProvider {

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ADMIN = "admin";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generate(AuthenticatedUser authenticatedUser) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
                .subject(String.valueOf(authenticatedUser.id()))
                .claim(CLAIM_USERNAME, authenticatedUser.username())
                .claim(CLAIM_ADMIN, authenticatedUser.admin())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public AuthenticatedUser resolveAuthenticatedUser(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long id = Long.parseLong(claims.getSubject());
            String username = claims.get(CLAIM_USERNAME, String.class);
            Boolean admin = claims.get(CLAIM_ADMIN, Boolean.class);

            if (username == null || admin == null) {
                throw new JwtException("Missing required JWT claims");
            }

            return new AuthenticatedUser(id, username, admin);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtException("Invalid JWT token", ex);
        }
    }
}
