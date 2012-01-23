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

        // if email is null
        String url = instance.createImgURL(null);
        // NullPointerException is catched by setting email to empty String (!= null)

        // if email is a empty string, same result as if email is null
        String urlEmpty = instance.createImgURL("");

        // show that there is no image to this email address
        assertNotNull(url);
        assertNotNull(urlEmpty);
        assertEquals(url, urlEmpty);
    }
}
