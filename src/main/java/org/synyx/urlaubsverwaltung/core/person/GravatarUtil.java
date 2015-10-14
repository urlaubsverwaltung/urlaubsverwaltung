package org.synyx.urlaubsverwaltung.core.person;

import org.apache.log4j.Logger;

import org.springframework.util.Assert;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * This class creates a Gravatar URL for a mail address.
 *
 * @author  Aljona Murygina
 */
public final class GravatarUtil {

    static final String BASE_URL = "https://www.gravatar.com/avatar/";

    private static final String DIGEST_ALGORITHM = "MD5";

    private static final Logger LOG = Logger.getLogger(GravatarUtil.class);

    private GravatarUtil() {

        // Hide constructor for util classes
    }

    /**
     * This method generates the complete Gravatar URL to the given mail address.
     *
     * @param  email  String
     *
     * @return  complete Gravatar URL
     */
    public static String createImgURL(String email) {

        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

        return BASE_URL + createHash(normalizedEmail);
    }


    private static String createHash(String string) {

        Assert.notNull("String to be encrypted may not be null", string);

        String encryptedString = null;

        try {
            MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);

            byte[] updatedData = md.digest(string.getBytes());
            encryptedString = convertToHex(updatedData);
        } catch (NoSuchAlgorithmException ex) {
            LOG.error("Creation of message digest failed.", ex);
        }

        return encryptedString;
    }


    private static String convertToHex(byte[] data) {

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < data.length; i++) {
            int halfByte = (data[i] >>> 4) & 0x0F; // NOSONAR
            int twoHalves = 0;

            do {
                if ((0 <= halfByte) && (halfByte <= 9)) { // NOSONAR
                    buf.append((char) ('0' + halfByte));
                } else {
                    buf.append((char) ('a' + (halfByte - 10))); // NOSONAR
                }

                halfByte = data[i] & 0x0F; // NOSONAR
            } while (twoHalves++ < 1);
        }

        return buf.toString();
    }
}
