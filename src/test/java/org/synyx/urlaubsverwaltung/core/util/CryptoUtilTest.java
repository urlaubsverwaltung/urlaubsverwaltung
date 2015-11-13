/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.core.util;

import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.util.CryptoUtil;

import java.security.KeyPair;
import java.security.PrivateKey;

import java.util.Arrays;

import static org.junit.Assert.*;


/**
 * @author  Aljona Murygina
 */
public class CryptoUtilTest {

    @Test
    public void ensureCanGenerateKeyPair() throws Exception {

        KeyPair keyPair = CryptoUtil.generateKeyPair();

        assertNotNull("Should not be null", keyPair);
    }


    @Test
    public void ensureSigningDataChangesTheData() throws Exception {

        PrivateKey privateKey = CryptoUtil.generateKeyPair().getPrivate();

        byte[] data = "Lorem ipsum".getBytes();

        byte[] signedData = CryptoUtil.sign(privateKey, data);

        assertNotNull("Should not be null", signedData);
        assertNotEquals("Data should have changed after signing", data, signedData);
    }


    @Test
    public void ensureCanGetPrivateKeyFromBytes() throws Exception {

        PrivateKey privateKey = CryptoUtil.generateKeyPair().getPrivate();

        byte[] encodedPrivateKey = privateKey.getEncoded();

        PrivateKey privateKeyAsBytes = CryptoUtil.getPrivateKeyByBytes(encodedPrivateKey);

        assertNotNull("Should not be null", privateKeyAsBytes);
        assertEquals("Wrong private key", privateKey, privateKeyAsBytes);
    }
}
