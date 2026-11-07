package com.festa.user.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.festa.user.api.security.FestaJwtAuthenticationConverter;
import com.festa.user.api.security.FestaPrincipal;
import com.festa.user.integration.TestKeys;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Firma el token igual que festa-auth-service (TokenService.accessToken: RS256, iss, sub, role)
 * y verifica que user-service lo acepte con la llave pública en PEM, como la monta festa-infra.
 */
class AuthTokenCompatibilityTest {

    private static final String ISSUER = "festa-auth";

    @Test
    void aceptaElTokenDeAuthYLeeSubYRol() throws Exception {
        UUID accountId = UUID.randomUUID();
        JwtDecoder decoder = new SecurityConfig().jwtDecoder(TestKeys.publicKeyPem(), ISSUER);

        var jwt = decoder.decode(token(TestKeys.PAIR, ISSUER, accountId, "merchant"));
        var auth = new FestaJwtAuthenticationConverter().convert(jwt);

        assertThat(((FestaPrincipal) auth.getPrincipal()).accountId().value()).isEqualTo(accountId);
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_MERCHANT");
    }

    @Test
    void rechazaOtroEmisorUOtraLlave() throws Exception {
        JwtDecoder decoder = new SecurityConfig().jwtDecoder(TestKeys.publicKeyPem(), ISSUER);
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        assertThatThrownBy(() -> decoder.decode(token(TestKeys.PAIR, "otro-emisor", UUID.randomUUID(), "user")))
                .isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> decoder.decode(token(generator.generateKeyPair(), ISSUER, UUID.randomUUID(), "user")))
                .isInstanceOf(JwtException.class);
    }

    private static String token(KeyPair pair, String issuer, UUID subject, String role) {
        RSAKey jwk = new RSAKey.Builder((RSAPublicKey) pair.getPublic())
                .privateKey((RSAPrivateKey) pair.getPrivate()).keyID("festa-auth-1").build();
        var encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(subject.toString())
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(15)))
                .id(UUID.randomUUID().toString())
                .claim("email", "ana@mail.escuelaing.edu.co")
                .claim("role", role)
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId("festa-auth-1").type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
