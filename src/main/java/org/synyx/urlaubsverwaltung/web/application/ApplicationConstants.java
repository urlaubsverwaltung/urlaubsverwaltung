
package org.synyx.urlaubsverwaltung.web.application;

import org.synyx.urlaubsverwaltung.web.ControllerConstants;


/**
 * Constants concerning {@link ApplicationController}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationConstants {

    // attribute names
    public static final String COMMENT = "comment";
    public static final String APPFORM = "appForm";
    public static final String PERSON_LIST = "personList"; // office can apply for leave for this persons
    public static final String NOTPOSSIBLE = "notpossible"; // is it possible for user to apply for leave?

    // he/she has no account/entitlement)
    public static final String APPLICATION_ID = "applicationId";
    public static final String PERSON_ID = "personId";

    // jsps
    public static final String APP_LIST_JSP = ControllerConstants.APPLICATION + "/app_list";
    public static final String SHOW_APP_DETAIL_JSP = ControllerConstants.APPLICATION + "/app_detail";
    public static final String APP_FORM_JSP = ControllerConstants.APPLICATION + "/app_form";

    // applications' status
    // title in list jsp
    public static final String TITLE_APP = "titleApp";
    public static final String TITLE_WAITING = "applications.waiting";
    public static final String TITLE_ALLOWED = "applications.allowed";
    public static final String TITLE_REJECTED = "applications.rejected";
    public static final String TITLE_CANCELLED = "applications.cancelled";
    public static final String TOUCHED_DATE = "touchedDate";
    public static final String DATE_OVERVIEW = "app.date.overview";
    public static final String DATE_APPLIED = "app.date.applied";
    public static final String DATE_ALLOWED = "app.date.allowed";
    public static final String DATE_REJECTED = "app.date.rejected";
    public static final String DATE_CANCELLED = "app.date.cancelled";
}
