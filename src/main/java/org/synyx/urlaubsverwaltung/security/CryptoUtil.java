package org.synyx.urlaubsverwaltung.security;

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
 * this class contains all methods that deal with private and public keys and signature it is able to: generate private
 * and public keys get in database as byte[] saved keys and convert them back to rsa keys sign data with private key and
 * verify signed data with public key
 *
 * @author  Aljona Murygina
 */
public final class CryptoUtil {

    private static final int KEYSIZE = 2048;

    private CryptoUtil() {

        // Hide constructor for util classes
    }

    /**
     * generates a key pair (private key, public key).
     *
     * @return  KeyPair
     *
     * @throws  NoSuchAlgorithmException
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {

        KeyPairGenerator pairgen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        pairgen.initialize(KEYSIZE, random);

        return pairgen.generateKeyPair();
    }


    /**
     * A Signature object may have three states: UNINITIALIZED SIGN VERIFY
     *
     * <p>First created: a Signature object is in the UNINITIALIZED state. There are two initialization methods:
     * initSign and initVerify, which change the state to SIGN or to VERIFY.</p>
     *
     * <p>With the private key you sign the data, with the public key you verify the signed data.</p>
     *
     * @param  privKey
     * @param  originData
     *
     * @return  signed data {byte[]}
     *
     * @throws  NoSuchAlgorithmException
     */
    public static byte[] sign(PrivateKey privKey, byte[] originData) throws NoSuchAlgorithmException,
        InvalidKeyException, SignatureException {

        Signature sign = Signature.getInstance("SHA256withRSA");
        byte[] updatedData = null;

        /* Initializing the object with a private key */
        sign.initSign(privKey);

        /* Update and sign the data */
        sign.update(originData);

        updatedData = sign.sign();

        return updatedData;
    }


    /**
     * takes bytes of PrivateKey saved in database and converts back to PrivateKey.
     *
     * @param  savedKey
     *
     * @return
     *
     * @throws  NoSuchAlgorithmException
     * @throws  InvalidKeySpecException
     */
    public static PrivateKey getPrivateKeyByBytes(byte[] savedKey) throws NoSuchAlgorithmException,
        InvalidKeySpecException {

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(savedKey);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PrivateKey privKey = keyFactory.generatePrivate(keySpec);

        return privKey;
    }


    // TODO: To be used as soon as all missing features for database authentication are implemented!
    /**
     * Generates password with 16 characters length with spring standard key generator.
     *
     * @return  password with 16 characters
     */
    public static String generatePassword() {

        StringKeyGenerator generator = KeyGenerators.string();

        return generator.generateKey();
    }


    /**
     * Encodes a given raw password with random salt via Spring StandardPasswordEncoder.
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
