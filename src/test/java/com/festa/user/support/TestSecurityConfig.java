package com.festa.user.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;

/**
 * Reemplaza el decoder JWKS en pruebas: el token es "accountId.rol" (p. ej. "3f2c....merchant")
 * y se acepta sin firma. También acepta "accountId:rol", pero Spring rechaza los dos puntos en un
 * Bearer real (solo letras, dígitos y -._~+/), así que las pruebas por HTTP usan el punto.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtDecoder testJwtDecoder() {
        return token -> {
            int sep = token.contains(":") ? token.indexOf(':') : token.lastIndexOf('.');
            String[] parts = sep < 0 ? new String[] {token} : new String[] {token.substring(0, sep), token.substring(sep + 1)};
            Instant now = Instant.now();
            return Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .subject(parts[0])
                    .claim("role", parts.length > 1 ? parts[1] : "user")
                    .issuer("festa-auth")
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build();
        };
    }
}
