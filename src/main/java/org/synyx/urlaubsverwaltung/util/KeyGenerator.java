package org.synyx.urlaubsverwaltung.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;


public class KeyGenerator {

    private static final int KEYSIZE = 2048;

    public PrivateKey generateKey() throws NoSuchAlgorithmException {

        KeyPairGenerator pairgen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        pairgen.initialize(KEYSIZE, random);

        KeyPair keyPair = pairgen.generateKeyPair();

        return keyPair.getPrivate();
    }


    public String getFingerprint() throws NoSuchAlgorithmException {

        String privKey = generateKey().toString();

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(privKey.getBytes());

        byte[] output = md.digest();

        char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        StringBuffer buf = new StringBuffer();

        for (int j = 0; j < output.length; j++) {
            buf.append(hexDigit[(output[j] >> 4) & 0x0f]);
            buf.append(hexDigit[output[j] & 0x0f]);
        }

        return buf.toString();
    }

//   public static void sign() throws NoSuchAlgorithmException {
//
//       Signature srsa = Signature.getInstance("SHA256withRSA");
//
//       /* Initializing the object with a private key */
//        PrivateKey privKey = generateKey();
//        srsa.initSign(privKey);
//
//        /* Update and sign the data */
//        srsa.update(data);
//        byte[] sig = srsa.sign();
//
//   }
}
