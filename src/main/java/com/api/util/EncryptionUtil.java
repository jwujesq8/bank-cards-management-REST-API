package com.api.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Base64;

/**
 * Class EncryptionUtil
 *
 * Utility class for encrypting and decrypting text using AES algorithm.
 *
 * <p>This class provides simple AES encryption and decryption using a fixed secret key.
 */
@Component
public class EncryptionUtil {

    /**
     * Secret key used for AES encryption/decryption (must be 16 bytes for AES-128).
     */
    private final String SECRET_KEY;
    /**
     * Encryption algorithm used.
     */
    private static final String ALGORITHM = "AES";

    /**
     * Constructor for EncryptionUtil.
     *
     * Read secret key from a filepath that is defined in the application.properties
     */
    public EncryptionUtil(
            @Value("${secret.key.path}") String secretKeyPath,
            ResourceLoader resourceLoader
    ) throws IOException {

        // SECRET_KEY
        Resource secretResource = resourceLoader.getResource(secretKeyPath);
        if (secretResource.exists()) {
            SECRET_KEY = new String(secretResource.getInputStream().readAllBytes());
        } else {
            throw new IOException("Access file not found: " + secretKeyPath.toString());
        }
    }

    /**
     * Encrypts a plain text string using AES encryption and encodes the result as a Base64 string.
     *
     * @param plainText the original plain text to encrypt
     * @return the encrypted text encoded in Base64
     * @throws RuntimeException if encryption fails
     */
    public String encrypt(String plainText){
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    /**
     * Decrypts a Base64-encoded encrypted string using AES decryption.
     *
     * @param encryptedText the encrypted text in Base64 format
     * @return the decrypted original plain text
     * @throws RuntimeException if decryption fails
     */
    public String decrypt(String encryptedText){
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}
