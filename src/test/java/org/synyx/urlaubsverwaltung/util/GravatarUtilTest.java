/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.util;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author  Aljona Murygina
 */
public class GravatarUtilTest {

    private static final String BASE_URL = "http://www.gravatar.com/avatar/";

    private GravatarUtil instance;

    public GravatarUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

        instance = new GravatarUtil();
    }


    @After
    public void tearDown() {
    }


    /** Test of createImgURL method, of class GravatarUtil. */
    @Test
    public void testCreateImgURL() {

        String email = "frau.lyoner@net.de";

        String emailWithUpperCase = "FraU.LyOner@net.de";

        String emailWithWhitespaces = " fraU.Lyoner@Net.de";

        // show that given email is trimmed and lowercased
        // that means: every url is equals

        String url1 = instance.createImgURL(email);
        String url2 = instance.createImgURL(emailWithUpperCase);
        String url3 = instance.createImgURL(emailWithWhitespaces);

        assertNotNull(url1);
        assertNotNull(url2);
        assertNotNull(url3);

        assertEquals(url1, url2);
        assertEquals(url1, url3);
        assertEquals(url2, url3);

        // show that the BASE_URL is part of the url
        assertTrue(url1.contains(BASE_URL));

        // show that the given email is not part of the url but a hash
        assertFalse(url1.contains(email));
    }

//    /** Test of createHash method, of class GravatarUtil. */
//    @Test
//    public void testCreateHash() {
//
//        String email = "frau.lyoner@net.de";
//
//        String emailWithUpperCase = "FraU.LyOner@net.de";
//
//        String emailWithWhitespaces = " fraU.Lyoner@Net.de";
//
//        // show that given email is trimmed and lowercased
//        // that means: every hash is equals
//
//        String encrypt1 = instance.createHash(email);
//        String encrypt2 = instance.createHash(emailWithUpperCase);
//        String encrypt3 = instance.createHash(emailWithWhitespaces);
//
//        assertNotNull(encrypt1);
//        assertNotNull(encrypt2);
//        assertNotNull(encrypt3);
//
//        assertEquals(encrypt1, encrypt2);
//
//        // WTF??
//        assertNotSame(encrypt1, encrypt2);
//
//        assertEquals(encrypt1, encrypt3);
//        assertEquals(encrypt2, encrypt3);
//
//        // origin emails have been encrypted, are not same
//        assertNotSame(email, encrypt1);
//        assertNotSame(emailWithUpperCase, encrypt2);
//        assertNotSame(emailWithWhitespaces, encrypt3);
//    }
}
