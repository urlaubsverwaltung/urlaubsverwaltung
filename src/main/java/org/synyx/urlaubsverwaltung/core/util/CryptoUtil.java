package org.synyx.urlaubsverwaltung.core.util;

import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;


/**
 * Contains all crypto relevant util methods, like generating key pair for person or signing with key pair.
 *
 * @author  Aljona Murygina
 */
public final class CryptoUtil {

    private static final int KEY_SIZE = 2048;
    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private CryptoUtil() {

        // Hide constructor for util classes
    }

    /**
     * Generates a key pair: private key and public key.
     *
     * @return  created key pair
     *
     * @throws  NoSuchAlgorithmException  in case the chosen algorithm does not exist
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        SecureRandom random = new SecureRandom();
        keyPairGenerator.initialize(KEY_SIZE, random);

        return keyPairGenerator.generateKeyPair();
    }


    /**
     * Signs the given data with the given private key.
     *
     * <p>NOTE: With the private key you sign the data, with the public key you verify the signed data.</p>
     *
     * @param  privateKey  to sign the data with
     * @param  originData  to be signed
     *
     * @return  signed data as {@link byte[]}
     *
     * @throws  NoSuchAlgorithmException  in case the chosen algorithm does not exist
     */
    public static byte[] sign(PrivateKey privateKey, byte[] originData) throws NoSuchAlgorithmException,
        InvalidKeyException, SignatureException {

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);

        /* Initializing the object with a private key */
        signature.initSign(privateKey);

        /* Update and sign the data */
        signature.update(originData);

        return signature.sign();
    }


    /**
     * Converts bytes back to private key object.
     *
     * @param  privateKeyBytes  representing the private key in bytes
     *
     * @return  the private key
     *
     * @throws  NoSuchAlgorithmException  in case the chosen algorithm does not exist
     * @throws  InvalidKeySpecException  in case the key spec. is invalid
     */
    public static PrivateKey getPrivateKeyByBytes(byte[] privateKeyBytes) throws NoSuchAlgorithmException,
        InvalidKeySpecException {

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

        return keyFactory.generatePrivate(keySpec);
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
