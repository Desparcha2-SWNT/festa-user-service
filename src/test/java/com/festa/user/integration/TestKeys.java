package com.festa.user.integration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Par RSA de prueba, con la pública en un PEM temporal como el de festa-infra/secrets, y
 * tokens firmados igual que festa-auth-service (TokenService.accessToken).
 */
public final class TestKeys {

    public static final String ISSUER = "festa-auth";
    public static final KeyPair PAIR = generate();
    private static Path pem;

    private TestKeys() {
    }

    public static synchronized String publicKeyPem() {
        try {
            if (pem == null) {
                String body = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(PAIR.getPublic().getEncoded());
                pem = Files.createTempFile("jwt_public", ".pem");
                Files.writeString(pem, "-----BEGIN PUBLIC KEY-----\n" + body + "\n-----END PUBLIC KEY-----\n");
                pem.toFile().deleteOnExit();
            }
            return pem.toString();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static String token(UUID subject, String role) {
        return token(PAIR, ISSUER, subject, role);
    }

    public static String token(KeyPair pair, String issuer, UUID subject, String role) {
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

    public static KeyPair generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
