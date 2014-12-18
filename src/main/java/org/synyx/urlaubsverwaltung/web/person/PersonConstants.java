
package org.synyx.urlaubsverwaltung.web.person;

/**
 * Constants concerning {@link PersonController}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PersonConstants {

    // jsps
    public static final String OVERVIEW_JSP = "person/overview"; // jsp for personal overview
    public static final String STAFF_JSP = "person/staff_view";
    public static final String PERSON_FORM_JSP = "person/person_form";

    // attribute names
    public static final String LOGGED_USER = "loggedUser";
    public static final String PERSONFORM = "personForm";
    public static final String LEFT_DAYS = "leftDays";
    public static final String REM_LEFT_DAYS = "remLeftDays";
    public static final String BEFORE_APRIL = "beforeApril";
    public static final String GRAVATAR = "gravatar";
    public static final String GRAVATAR_URLS = "gravatarUrls";
    public static final String NOTEXISTENT = "notexistent"; // are there any persons to show?
    public static final String NO_APPS = "noapps"; // are there any applications to show?
    public static final String PERSON_ID = "personId";

    private PersonConstants() {

        // Hide constructor for util classes
    }
}
