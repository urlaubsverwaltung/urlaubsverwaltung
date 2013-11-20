/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class creates with given email address the url of the person's gravatar.
 *
 * @author  Aljona Murygina
 */
public class GravatarUtil {

    private static final String BASE_URL = "http://www.gravatar.com/avatar/";

    // logs general errors
    private static final Logger LOG = Logger.getLogger("errorLog");

    /**
     * This method generates the complete gravatar's url by the given email address.
     *
     * @param  email  String
     *
     * @return  complete url of the gravatar
     */
    public String createImgURL(String email) {

        if (email == null) {
            // set email to empty String so that no NullpointerException occurs, but a default image can be set
            email = "";
        }

        String hash = createHash(email);

        return BASE_URL + hash;
    }


    /**
     * This method creates a md5 hash of the given email.
     *
     * @param  email  String
     *
     * @return  encrypted email address
     */
    private String createHash(String email) {

        String encryptEmail = null;

        email = email.trim().toLowerCase();

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] updatedData = md.digest(email.getBytes());
            encryptEmail = convertToHex(updatedData);
        } catch (NoSuchAlgorithmException ex) {
            LOG.log(Level.WARNING, "Creation of MessageDigest failed.", ex);
        }

        return encryptEmail;
    }


    /**
     * This method converts the given bytes to hex.
     *
     * @param  data
     *
     * @return  String of bytes that have been converted to hex.
     */
    private String convertToHex(byte[] data) {

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;

            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }

                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }

        return buf.toString();
    }
}
