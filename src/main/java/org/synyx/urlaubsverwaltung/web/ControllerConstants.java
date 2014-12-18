
package org.synyx.urlaubsverwaltung.web;

/**
 * Constants concerning all controllers: {@link org.synyx.urlaubsverwaltung.web.person.PersonController} and
 * {@link org.synyx.urlaubsverwaltung.web.application.ApplicationController}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ControllerConstants {

    public static final String LOGIN_LINK = "redirect:/login.jsp?login_error=1";

    public static final String YEAR = "year";

    public static final String PERSON = "person";
    public static final String PERSONS = "persons";

    public static final String APPLICATION = "application";
    public static final String APPLICATIONS = "applications";

    public static final String ACCOUNT = "account";
    public static final String ACCOUNTS = "accounts";

    public static final String ERROR_JSP = "error";

    private ControllerConstants() {

        // Hide constructor for util classes
    }
}
