package org.synyx.urlaubsverwaltung.service;

import sun.misc.BASE64Encoder;

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

import java.util.logging.Level;
import java.util.logging.Logger;


public class PGPService {

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
    public byte[] sign(PrivateKey privKey, byte[] ursprungsData) throws NoSuchAlgorithmException {

        Signature sign = Signature.getInstance("SHA256withRSA");
        byte[] updatedData = null;

        try {
            /* Initializing the object with a private key */
            sign.initSign(privKey);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(PGPService.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            /* Update and sign the data */
            sign.update(ursprungsData);
        } catch (SignatureException ex) {
            Logger.getLogger(PGPService.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            updatedData = sign.sign();
        } catch (SignatureException ex) {
            Logger.getLogger(PGPService.class.getName()).log(Level.SEVERE, null, ex);
        }

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


    /**
     * just prints encoded signature
     *
     * @param  signature
     */
    public void printSignature(byte[] signature) {

        System.out.println(new BASE64Encoder().encode(signature));
    }


    /**
     * get encoded signature
     *
     * @param  signature
     *
     * @return  String with signature
     */
    public String getEncodedSignature(byte[] signature) {

        return (new BASE64Encoder().encode(signature));
    }
}
