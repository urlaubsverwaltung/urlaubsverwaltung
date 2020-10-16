package org.synyx.urlaubsverwaltung.person;

import org.slf4j.Logger;

import java.security.NoSuchAlgorithmException;

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.security.MessageDigest.getInstance;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class creates a Gravatar URL for a mail address.
 */
final class GravatarUtil {

    private static final String GRAVATAR_BASE_URL = "https://gravatar.com/avatar/";

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private GravatarUtil() {
        // Hide constructor for util classes
    }

    /**
     * This method generates the complete Gravatar URL to the given mail address.
     *
     * @param email String
     * @return complete Gravatar URL
     */
    static String createImgURL(String email) {

        final String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

        String encryptedEmail = null;
        try {
            encryptedEmail = encodeHexString(getInstance("MD5").digest(normalizedEmail.getBytes(UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            LOG.error("Creation of message digest failed.", ex);
        }

        return GRAVATAR_BASE_URL + encryptedEmail;
    }
}
