package com.company.xmlgen.authentication.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT configuration. Single source of truth for token signing settings.
 */
@ConfigurationProperties(prefix = "xmlgen.jwt")
public class JwtProperties {

    private String secret;
    private long expirationMs = 86_400_000L;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}
