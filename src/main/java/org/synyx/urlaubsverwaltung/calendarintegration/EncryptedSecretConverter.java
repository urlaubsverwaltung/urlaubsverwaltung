package org.synyx.urlaubsverwaltung.calendarintegration;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Encrypts sensitive calendar integration values (google oauth client secret, access token and refresh token)
 * with AES-256-GCM before they are written to the database.
 * <p>
 * The encryption key is derived (PBKDF2) from the configured {@code uv.calendar-integration.encryption-secret}
 * and a random salt that is generated per value and stored as part of the encrypted payload:
 * {@code enc:v1:base64(salt || iv || ciphertext)}.
 * <p>
 * Encryption is only active when {@code uv.calendar-integration.encryption-secret} is configured.
 * Legacy plaintext values are read as-is and become encrypted with the next save.
 */
@Component
@Converter
class EncryptedSecretConverter implements AttributeConverter<String, String> {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final String ENCRYPTED_PREFIX = "enc:v1:";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int SALT_LENGTH = 16;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int KEY_DERIVATION_ITERATIONS = 65536;
    private static final int MAX_CACHED_KEYS = 100;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, SecretKeySpec> keyCache = new ConcurrentHashMap<>();
    private final String encryptionSecret;

    EncryptedSecretConverter(CalendarIntegrationProperties properties) {
        final String secret = properties.getEncryptionSecret();
        this.encryptionSecret = secret == null || secret.isBlank() ? null : secret;
        if (this.encryptionSecret == null) {
            LOG.warn("No encryption secret configured (uv.calendar-integration.encryption-secret) - " +
                "calendar integration secrets like oauth tokens will be stored in plaintext.");
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {

        if (attribute == null || encryptionSecret == null) {
            return attribute;
        }

        try {
            final byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);
            final byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            final Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, deriveKey(salt), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            final byte[] cipherText = cipher.doFinal(attribute.getBytes(UTF_8));

            final byte[] payload = ByteBuffer.allocate(salt.length + iv.length + cipherText.length)
                .put(salt)
                .put(iv)
                .put(cipherText)
                .array();

            return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not encrypt calendar integration secret", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {

        if (dbData == null || !dbData.startsWith(ENCRYPTED_PREFIX)) {
            // plaintext value (legacy or encryption not enabled) - encrypted with the next save
            return dbData;
        }

        if (encryptionSecret == null) {
            throw new IllegalStateException("Found an encrypted calendar integration secret but no encryption secret " +
                "is configured. Please configure 'uv.calendar-integration.encryption-secret' with the secret that was " +
                "used for encryption.");
        }

        try {
            final byte[] payload = Base64.getDecoder().decode(dbData.substring(ENCRYPTED_PREFIX.length()));

            final byte[] salt = new byte[SALT_LENGTH];
            ByteBuffer.wrap(payload).get(salt);

            final Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(salt), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, payload, SALT_LENGTH, GCM_IV_LENGTH));
            final int cipherTextOffset = SALT_LENGTH + GCM_IV_LENGTH;
            final byte[] plainText = cipher.doFinal(payload, cipherTextOffset, payload.length - cipherTextOffset);

            return new String(plainText, UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new IllegalStateException("Could not decrypt calendar integration secret - " +
                "was 'uv.calendar-integration.encryption-secret' changed?", e);
        }
    }

    private SecretKeySpec deriveKey(byte[] salt) {

        // key derivation is expensive - cache derived keys by salt
        if (keyCache.size() > MAX_CACHED_KEYS) {
            keyCache.clear();
        }

        return keyCache.computeIfAbsent(Base64.getEncoder().encodeToString(salt), ignored -> {
            try {
                final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                final PBEKeySpec spec = new PBEKeySpec(encryptionSecret.toCharArray(), salt, KEY_DERIVATION_ITERATIONS, 256);
                return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("Could not derive encryption key from 'uv.calendar-integration.encryption-secret'", e);
            }
        });
    }
}
