/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.security;

import org.junit.Test;

import java.security.KeyPair;
import java.security.PrivateKey;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;


/**
 * @author  Aljona Murygina
 */
public class CryptoUtilTest {

    /**
     * Test of generateKeyPair method, of class CryptoUtil.
     */
    @Test
    public void testGenerateKeyPair() throws Exception {

        KeyPair returnValue = CryptoUtil.generateKeyPair();

        assertNotNull(returnValue);

        assertEquals(KeyPair.class, returnValue.getClass());
    }


    /**
     * Test of sign method, of class CryptoUtil.
     */
    @Test
    public void testSign() throws Exception {

        PrivateKey privKey = CryptoUtil.generateKeyPair().getPrivate();

        byte[] data = "DiesIstEinBeispielFuerByteErzeugung".getBytes();

        byte[] returnValue = CryptoUtil.sign(privKey, data);

        assertNotNull(returnValue);

        // byte[] wurde veraendert durch signieren
        assertFalse(Arrays.equals(data, returnValue));
    }


    /**
     * Test of getPrivateKeyByBytes method, of class CryptoUtil.
     */
    @Test
    public void testGetPrivateKeyByBytes() throws Exception {

        PrivateKey privKey = CryptoUtil.generateKeyPair().getPrivate();

        byte[] keyData = privKey.getEncoded();

        PrivateKey returnValue = CryptoUtil.getPrivateKeyByBytes(keyData);

        assertNotNull(returnValue);
        assertEquals(privKey, returnValue);
    }
}
