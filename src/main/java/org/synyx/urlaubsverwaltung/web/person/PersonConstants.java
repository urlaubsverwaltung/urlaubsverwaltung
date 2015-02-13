
package org.synyx.urlaubsverwaltung.web.person;

/**
 * Constants concerning {@link PersonController}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public final class PersonConstants {

    public static final String STAFF_JSP = "person/staff_view";
    public static final String PERSON_FORM_JSP = "person/person_form";

    // attribute names
    public static final String LOGGED_USER = "loggedUser";
    public static final String LEFT_DAYS = "leftDays";
    public static final String REM_LEFT_DAYS = "remLeftDays";
    public static final String BEFORE_APRIL = "beforeApril";
    public static final String GRAVATAR_URLS = "gravatarUrls";

    private PersonConstants() {

        // Hide constructor for util classes
    }
}
