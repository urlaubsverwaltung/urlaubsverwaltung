/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sun.security.rsa.RSAPrivateCrtKeyImpl;
import sun.security.rsa.RSAPublicKeyImpl;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import java.util.Arrays;


/**
 * tests functionality of cryptoService
 *
 * @author  Aljona Murygina
 */
public class CryptoServiceTest {

    private CryptoService instance;

    public CryptoServiceTest() {
    }

    // vor Bauen der Klasse
    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    // nach Klasse
    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    // wird vor jedem Test neu gemacht
    @Before
    public void setUp() {

        instance = new CryptoService();
    }


    // nach jedem Test
    @After
    public void tearDown() {
    }


    /** Test of generateKeyPair method, of class CryptoService. */
    @Test
    public void testGenerateKeyPair() throws Exception {

        KeyPair returnValue = instance.generateKeyPair();

        assertNotNull(returnValue);

        assertEquals(RSAPrivateCrtKeyImpl.class, returnValue.getPrivate().getClass());
        assertEquals(RSAPublicKeyImpl.class, returnValue.getPublic().getClass());
    }


    /** Test of sign method, of class CryptoService. */
    @Test
    public void testSign() throws Exception {

        PrivateKey privKey = instance.generateKeyPair().getPrivate();

        byte[] data = "DiesIstEinBeispielFuerByteErzeugung".getBytes();

        byte[] returnValue = instance.sign(privKey, data);

        assertNotNull(returnValue);

        // byte[] wurde veraendert durch signieren
        assertFalse(Arrays.equals(data, returnValue));
    }


    /** Test of verify method, of class CryptoService. */
    @Test
    public void testVerify() throws Exception {

        // erstes erzeugtes KeyPair
        KeyPair pair1 = instance.generateKeyPair();
        PrivateKey privKey1 = pair1.getPrivate();
        PublicKey pubKey1 = pair1.getPublic();

        byte[] ursprungsData = "DiesIstEinBeispielFuerByteErzeugung".getBytes();

        // aus ursprungsData wird Signatur erzeugt
        byte[] signature1 = instance.sign(privKey1, ursprungsData);

        // teste das verifizieren mit erstem erzeugten KeyPair

        boolean returnValue1 = instance.verify(ursprungsData, signature1, pubKey1);

        assertNotNull(returnValue1);
        assertTrue(returnValue1);

        // erzeuge zweites KeyPair
        KeyPair pair2 = instance.generateKeyPair();
        PublicKey pubKey2 = pair2.getPublic();

        // nehme die signatur (signatur1), die mit dem ersten private key (privKey1) erzeugt wurde
        // und verifiziere mit dem zweiten public key (pubKey2)

        boolean returnValue2 = instance.verify(ursprungsData, signature1, pubKey2);

        assertNotNull(returnValue2);
        assertFalse(returnValue2);
    }


    /** Test of getPrivateKeyByBytes method, of class CryptoService. */
    @Test
    public void testGetPrivateKeyByBytes() throws Exception {

        PrivateKey privKey = instance.generateKeyPair().getPrivate();

        byte[] keyData = privKey.getEncoded();

        PrivateKey returnValue = instance.getPrivateKeyByBytes(keyData);

        assertNotNull(returnValue);
        assertEquals(privKey, returnValue);
    }


    /** Test of getPublicKeyByBytes method, of class CryptoService. */
    @Test
    public void testGetPublicKeyByBytes() throws Exception {

        PublicKey pubKey = instance.generateKeyPair().getPublic();

        byte[] keyData = pubKey.getEncoded();

        PublicKey returnValue = instance.getPublicKeyByBytes(keyData);

        assertNotNull(returnValue);
        assertEquals(pubKey, returnValue);
    }
}
