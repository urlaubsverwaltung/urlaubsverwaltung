package org.synyx.urlaubsverwaltung.web.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.log4j.Logger;

import org.springframework.util.Assert;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Util class for encryption.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptionUtil {

    private static final Logger LOG = Logger.getLogger(GravatarUtil.class);

    private static final String DIGEST_ALGORITHM = "MD5";

    /**
     * This method creates a md5 hash of the given string.
     *
     * @param  string  to be encrypted
     *
     * @return  encrypted string
     */
    public static String createHash(String string) {

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


    /**
     * This method converts the given bytes to hex.
     *
     * @param  data  to be converted to hex
     *
     * @return  converted data
     */
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
