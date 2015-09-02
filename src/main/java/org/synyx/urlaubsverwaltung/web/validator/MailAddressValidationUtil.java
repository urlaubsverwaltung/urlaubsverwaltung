package org.synyx.urlaubsverwaltung.web.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Util class to validate mail address.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class MailAddressValidationUtil {

    // a regex for email addresses that are valid, but may be "strange looking" (e.g. tomr$2@example.com)
    // original from: http://www.markussipila.info/pub/emailvalidator.php?action=validate
    // modified by adding following characters: äöüß
    private static final String EMAIL_PATTERN =
        "^[a-zäöüß0-9,!#\\$%&'\\*\\+/=\\?\\^_`\\{\\|}~-]+(\\.[a-zäöüß0-9,!#\\$%&'\\*\\+/=\\?\\^_`\\{\\|}~-]+)*@"
        + "[a-zäöüß0-9-]+(\\.[a-zäöüß0-9-]+)*\\.([a-z]{2,})$";

    /**
     * Checks if the provided mail address has a valid format, for example `marlene@muster.de`.
     *
     * @param  mailAddress  to be checked
     *
     * @return  {@code true} if the provided mail address has a valid format, {@code false} else
     */
    public static boolean hasValidFormat(String mailAddress) {

        String normalizedEmail = mailAddress.trim().toLowerCase();

        return matchPattern(EMAIL_PATTERN, normalizedEmail);
    }


    private static boolean matchPattern(String nameOfPattern, String matchSequence) {

        Pattern pattern = Pattern.compile(nameOfPattern);
        Matcher matcher = pattern.matcher(matchSequence);

        return matcher.matches();
    }
}
