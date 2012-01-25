package org.synyx.urlaubsverwaltung.service;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


/**
 * this class contains all methods that deal with private and public keys and signature it is able to: generate private
 * and public keys get in database as byte[] saved keys and convert them back to rsa keys sign data with private key and
 * verify signed data with public key
 *
 * @author  Aljona Murygina
 */
public class CryptoService {

    private static final int KEYSIZE = 2048;

    /**
     * generates a key pair (private key, public key)
     *
     * @return  KeyPair
     *
     * @throws  NoSuchAlgorithmException
     */
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {

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
     * @param  data
     *
     * @return  signed data {byte[]}
     *
     * @throws  NoSuchAlgorithmException
     */
    public byte[] sign(PrivateKey privKey, byte[] ursprungsData) throws NoSuchAlgorithmException, InvalidKeyException,
        SignatureException {

        Signature sign = Signature.getInstance("SHA256withRSA");
        byte[] updatedData = null;

        /* Initializing the object with a private key */
        sign.initSign(privKey);

        /* Update and sign the data */
        sign.update(ursprungsData);

        updatedData = sign.sign();

        return updatedData;
    }


    /**
     * verifies if signature is valid (test with public key if signature is created with private key)
     *
     * @param  ursprungsData
     * @param  signatureToVerify
     * @param  pubKey
     *
     * @return
     *
     * @throws  NoSuchAlgorithmException
     * @throws  SignatureException
     * @throws  InvalidKeyException
     */
    public boolean verify(byte[] ursprungsData, byte[] signatureToVerify, PublicKey pubKey)
        throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        Signature sign = Signature.getInstance("SHA256withRSA");

        sign.initVerify(pubKey);
        sign.update(ursprungsData);

        return sign.verify(signatureToVerify);
    }


    /**
     * takes in database saved bytes of PrivateKey and converts back to PrivateKey
     *
     * @param  savedKey
     *
     * @return
     *
     * @throws  NoSuchAlgorithmException
     * @throws  InvalidKeySpecException
     */
    public PrivateKey getPrivateKeyByBytes(byte[] savedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(savedKey);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PrivateKey privKey = keyFactory.generatePrivate(keySpec);

        return privKey;
    }


    /**
     * takes in database saved bytes of PublicKey and converts back to PublicKey
     *
     * @param  savedKey
     *
     * @return
     *
     * @throws  NoSuchAlgorithmException
     * @throws  InvalidKeySpecException
     */
    public PublicKey getPublicKeyByBytes(byte[] savedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(savedKey);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        return pubKey;
    }
}
