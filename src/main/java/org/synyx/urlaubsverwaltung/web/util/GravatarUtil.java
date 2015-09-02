package org.synyx.urlaubsverwaltung.web.util;

/**
 * This class creates with given email address the url of the person's gravatar.
 *
 * @author  Aljona Murygina
 */
public final class GravatarUtil {

    static final String BASE_URL = "https://www.gravatar.com/avatar/";

    private GravatarUtil() {

        // Hide constructor for util classes
    }

    /**
     * This method generates the complete Gravatar's url by the given email address.
     *
     * @param  email  String
     *
     * @return  complete url of the gravatar
     */
    public static String createImgURL(String email) {

        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

        String hash = EncryptionUtil.createHash(normalizedEmail);

        return BASE_URL + hash;
    }
}
