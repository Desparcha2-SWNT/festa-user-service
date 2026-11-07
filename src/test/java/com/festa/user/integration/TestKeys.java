package com.festa.user.integration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

/** Par RSA de prueba, con la pública en un PEM temporal como el de festa-infra/secrets. */
public final class TestKeys {

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

    private static KeyPair generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
