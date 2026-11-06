package com.festa.user.domain.port.out;

/**
 * Cifra/descifra campos PII con AES-256-GCM (RNF4). Implementación en
 * infrastructure.crypto.AesGcmPiiEncryptor.
 *
 * Formato del ciphertext: base64( nonce(12B) || ciphertext || tag(16B) ).
 * El dominio nunca ve el formato; solo string -> string.
 */
public interface PiiEncryptorPort {
    String encrypt(String plaintext);
    String decrypt(String ciphertext);
}