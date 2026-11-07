package com.festa.user.infrastructure.crypto;

import com.festa.user.config.AppProperties;
import com.festa.user.domain.port.out.PiiEncryptorPort;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM. Formato del ciphertext: base64( nonce(12) || ciphertext || tag(16) ).
 * Llave derivada con SHA-256 del valor de festa.user.pii-key para aceptar
 * cualquier longitud de string y obtener 32 bytes exactos.
 * Si pii-key está vacía, el bean falla al arrancar.
 */
@Component
public class AesGcmPiiEncryptor implements PiiEncryptorPort {

    private static final int NONCE_LEN = 12;
    private static final int TAG_BITS = 128;

    private final AppProperties props;
    private final SecureRandom secureRandom = new SecureRandom();
    private SecretKeySpec key;

    public AesGcmPiiEncryptor(AppProperties props) {
        this.props = props;
    }

    @PostConstruct
    void init() {
        if (props.piiKey() == null || props.piiKey().isBlank()) {
            throw new IllegalStateException(
                    "festa.user.pii-key (PII_ENCRYPTION_KEY) es obligatoria");
        }
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(props.piiKey().getBytes(StandardCharsets.UTF_8));
            this.key = new SecretKeySpec(hash, "AES");
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo inicializar la llave AES", ex);
        }
    }

    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] nonce = new byte[NONCE_LEN];
            secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] out = new byte[nonce.length + ct.length];
            System.arraycopy(nonce, 0, out, 0, nonce.length);
            System.arraycopy(ct, 0, out, nonce.length, ct.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception ex) {
            throw new IllegalStateException("Fallo cifrando PII", ex);
        }
    }

    @Override
    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        try {
            byte[] raw = Base64.getDecoder().decode(ciphertext);
            byte[] nonce = new byte[NONCE_LEN];
            System.arraycopy(raw, 0, nonce, 0, NONCE_LEN);
            byte[] ct = new byte[raw.length - NONCE_LEN];
            System.arraycopy(raw, NONCE_LEN, ct, 0, ct.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
            return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Fallo descifrando PII", ex);
        }
    }
}