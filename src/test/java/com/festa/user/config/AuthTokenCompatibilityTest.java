package com.festa.user.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.festa.user.api.security.FestaJwtAuthenticationConverter;
import com.festa.user.api.security.FestaPrincipal;
import com.festa.user.integration.TestKeys;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * Firma el token igual que festa-auth-service (RS256, iss, sub, role) y verifica que
 * user-service lo acepte con la llave pública en PEM, como la monta festa-infra.
 */
class AuthTokenCompatibilityTest {

    @Test
    void aceptaElTokenDeAuthYLeeSubYRol() throws Exception {
        UUID accountId = UUID.randomUUID();
        JwtDecoder decoder = new SecurityConfig().jwtDecoder(TestKeys.publicKeyPem(), TestKeys.ISSUER);

        var jwt = decoder.decode(TestKeys.token(accountId, "merchant"));
        var auth = new FestaJwtAuthenticationConverter().convert(jwt);

        assertThat(((FestaPrincipal) auth.getPrincipal()).accountId().value()).isEqualTo(accountId);
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_MERCHANT");
    }

    @Test
    void rechazaOtroEmisorUOtraLlave() throws Exception {
        JwtDecoder decoder = new SecurityConfig().jwtDecoder(TestKeys.publicKeyPem(), TestKeys.ISSUER);

        assertThatThrownBy(() -> decoder.decode(TestKeys.token(TestKeys.PAIR, "otro-emisor", UUID.randomUUID(), "user")))
                .isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> decoder.decode(TestKeys.token(TestKeys.generate(), TestKeys.ISSUER, UUID.randomUUID(), "user")))
                .isInstanceOf(JwtException.class);
    }
}
