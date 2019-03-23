package org.synyx.urlaubsverwaltung.core.util;

import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.crypto.password.StandardPasswordEncoder;


/**
 * Contains all crypto relevant util methods, like generating key pair for person or signing with key pair.
 */
public final class CryptoUtil {

    private static final int KEY_SIZE = 2048;
    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private CryptoUtil() {

        // Hide constructor for util classes
    }

    // TODO: To be used as soon as all missing features for database authentication are implemented!
    /**
     * Generates password with 16 characters length with Spring standard key generator.
     *
     * @return  password with 16 characters
     */
    public static String generatePassword() {

        StringKeyGenerator generator = KeyGenerators.string();

        return generator.generateKey();
    }


    /**
     * Encodes a given raw password with random salt via Spring {@link StandardPasswordEncoder}.
     *
     * @param  rawPassword  plaintext password
     *
     * @return  encoded password
     */
    public static String encodePassword(String rawPassword) {

        /**
         * TODO: Think about to use a better password encoder
         *
         * Comment within StandardPasswordEncoder:
         *
         * If you are developing a new system,
         * {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder} is
         * a better choice both in terms of security and interoperability with other languages.
         */

        StandardPasswordEncoder encoder = new StandardPasswordEncoder();

        return encoder.encode(rawPassword);
    }
}
